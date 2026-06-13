package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.dto.ActualizarDireccionFavoritaRequest;
import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.DestinoPopularResponse;
import com.example.movilidadmdq.dto.DireccionFavoritaResponse;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.dto.ViajeHistorialResponse;
import com.example.movilidadmdq.repository.UsuarioRepository;
import com.example.movilidadmdq.service.FavoritoService;
import com.example.movilidadmdq.service.HistorialViajeService;
import com.example.movilidadmdq.service.ViajeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/* 
   CLASE: ViajeController
   
   Este controlador es el motor funcional de la aplicación. Maneja el cálculo de rutas, 
   la comparación de precios entre servicios (Taxi, Uber, Didi) y la gestión de 
   favoritos de cada usuario.
*/
@Tag(name = "Viajes", description = "Cálculo y comparación de opciones de transporte.")
@RequiredArgsConstructor
@RequestMapping("/viajes")
@RestController
public class ViajeController
{
    private final ViajeService viajeService;
    private final HistorialViajeService historialViajeService;
    private final FavoritoService favoritoService;
    private final UsuarioRepository usuarioRepository;

    /* 
       MÉTODO: getAuditoria (SOLO ADMIN)
       Permite a los administradores visualizar y filtrar todos los viajes registrados 
       en el sistema para control y monitoreo.
    */
    @Operation(summary = "Auditoría de viajes (ADMIN)")
    @GetMapping("/admin/auditoria")
    @PreAuthorize("hasRole('ADMIN')") // CANDADO: Solo permite acceso al rol administrador.
    public ResponseEntity<Page<ViajeHistorialResponse>> getAuditoria(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String origen,
            @RequestParam(required = false) String destino,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Boolean favorito,
            @PageableDefault(size = 10, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable) {
        
        // Se utiliza paginación para no sobrecargar el servidor con miles de registros.
        return ResponseEntity.ok(historialViajeService.obtenerAuditoriaViajes(username, origen, destino, desde, hasta, favorito, pageable));
    }

    /* 
       MÉTODO: getDestinosPopulares (SOLO ADMIN)
       Genera estadísticas sobre los lugares más consultados por los ciudadanos.
    */
    @Operation(summary = "Obtener destinos populares (ADMIN)")
    @GetMapping("/admin/destinos-populares")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DestinoPopularResponse>> getDestinosPopulares(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) String zona) {
        return ResponseEntity.ok(historialViajeService.obtenerDestinosPopulares(desde, hasta, zona));
    }

    /* 
       MÉTODO: calcular
       Es el núcleo de la aplicación. Recibe una solicitud de viaje y devuelve 
       la comparativa de precios y tiempos de llegada.
    */
    @Operation(summary = "Calcular viaje")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "El viaje se ha calculado con exito"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping("/calcular")
    public ResponseEntity<List<OpcionTransporteResponse>> calcular(
            @Valid @RequestBody CalculoViajeRequest request,
            Authentication authentication // Se inyecta la identidad del usuario logueado.
    )
    {
        // Verificación de seguridad básica.
        if (authentication == null || authentication.getName() == null)
        {
            return ResponseEntity.status(401).build();
        }

        // Obtiene el usuario del token y delega la lógica matemática al servicio.
        return usuarioRepository.findByUsername(authentication.getName())
                .map(usuario -> ResponseEntity.ok(viajeService.calcularViaje(request, usuario.getId())))
                .orElse(ResponseEntity.status(401).build());
    }

    /* 
       MÉTODO: toggleFavorito
       Marca o desmarca un viaje específico como favorito en el historial.
    */
    @PutMapping("/{viajeId}/favorito")
    public ResponseEntity<Void> toggleFavorito(
            @PathVariable Long viajeId,
            Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null)
        {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .map(usuario ->
                {
                    favoritoService.toggleFavorito(viajeId, usuario.getId());
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.status(401).build());
    }

    /* 
       MÉTODO: obtenerDireccionesFavoritas
       Devuelve los lugares guardados por el usuario (ej: "Trabajo", "Casa de mi abuela").
    */
    @GetMapping("/direcciones-favoritas")
    public ResponseEntity<List<DireccionFavoritaResponse>> obtenerDireccionesFavoritas(
            Authentication authentication
    )
    {
        if (authentication == null || authentication.getName() == null)
        {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .map(usuario -> ResponseEntity.ok(favoritoService.obtenerDireccionesFavoritas(usuario.getId())))
                .orElse(ResponseEntity.status(401).build());
    }

    /* 
       MÉTODO: renombrarDireccionFavorita
       Permite al usuario cambiar el nombre (alias) de una de sus direcciones guardadas.
    */
    @PutMapping("/direcciones-favoritas/{id}")
    public ResponseEntity<Void> renombrarDireccionFavorita(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarDireccionFavoritaRequest request,
            Authentication authentication
    )
    {
        if (authentication == null || authentication.getName() == null)
        {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .map(usuario ->
                {
                    favoritoService.renombrarDireccionFavorita(id, request.nombre(), usuario.getId());
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.status(401).build());
    }

    /* 
       MÉTODO: eliminarDireccionFavorita
       Elimina una dirección de la lista de favoritos del usuario.
    */
    @DeleteMapping("/direcciones-favoritas/{id}")
    public ResponseEntity<Void> eliminarDireccionFavorita(
            @PathVariable Long id,
            Authentication authentication
    )
    {
        if (authentication == null || authentication.getName() == null)
        {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .map(usuario ->
                {
                    favoritoService.eliminarDireccionFavorita(id, usuario.getId());
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.status(401).build());
    }

}
