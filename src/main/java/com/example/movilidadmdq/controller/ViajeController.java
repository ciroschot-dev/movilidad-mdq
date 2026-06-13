package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.dto.ActualizarDireccionFavoritaRequest;
import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.DestinoPopularResponse;
import com.example.movilidadmdq.dto.DireccionFavoritaResponse;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.dto.ViajeHistorialResponse;
import com.example.movilidadmdq.model.Usuario;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @PageableDefault(size = 10, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable)
    {
        // Se usa paginación para no traer miles de registros de una.
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
            @RequestParam(required = false) String zona)
    {
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
            @AuthenticationPrincipal Usuario usuario
    )
    {
        return ResponseEntity.ok(viajeService.calcularViaje(request, usuario.getId()));
    }

    /*
       MÉTODO: toggleFavorito
       Marca o desmarca un viaje específico como favorito en el historial.
    */
    @PutMapping("/{viajeId}/favorito")
    public ResponseEntity<Void> toggleFavorito(
            @PathVariable Long viajeId,
            @AuthenticationPrincipal Usuario usuario)
    {
        favoritoService.toggleFavorito(viajeId, usuario.getId());
        return ResponseEntity.ok().build();
    }

    /*
       MÉTODO: obtenerFavoritos
       Devuelve todos los viajes que el usuario marcó como favoritos.
    */
    @GetMapping("/favoritos")
    public ResponseEntity<List<ViajeHistorialResponse>> obtenerFavoritos(
            @AuthenticationPrincipal Usuario usuario
    )
    {
        return ResponseEntity.ok(
                favoritoService.obtenerFavoritos(usuario.getId()).stream()
                        .map(historialViajeService::toResponse)
                        .toList()
        );
    }

    /*
       MÉTODO: obtenerDireccionesFavoritas
       Devuelve los lugares guardados por el usuario (ej: "Trabajo", "Casa de mi abuela").
    */
    @Operation(summary = "Obtener direcciones favoritas", description = "Devuelve una lista única de todas las direcciones (orígenes y destinos) marcadas como favoritas.")
    @GetMapping("/direcciones-favoritas")
    public ResponseEntity<List<DireccionFavoritaResponse>> obtenerDireccionesFavoritas(
            @AuthenticationPrincipal Usuario usuario
    )
    {
        return ResponseEntity.ok(favoritoService.obtenerDireccionesFavoritas(usuario.getId()));
    }

    /*
       MÉTODO: renombrarDireccionFavorita
       Permite al usuario cambiar el nombre (alias) de una de sus direcciones guardadas.
    */
    @PutMapping("/direcciones-favoritas/{id}")
    public ResponseEntity<Void> renombrarDireccionFavorita(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarDireccionFavoritaRequest request,
            @AuthenticationPrincipal Usuario usuario
    )
    {
        favoritoService.renombrarDireccionFavorita(id, request.nombre(), usuario.getId());
        return ResponseEntity.ok().build();
    }

    /*
       MÉTODO: eliminarDireccionFavorita
       Elimina una dirección de la lista de favoritos del usuario.
    */
    @DeleteMapping("/direcciones-favoritas/{id}")
    public ResponseEntity<Void> eliminarDireccionFavorita(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario
    )
    {
        favoritoService.eliminarDireccionFavorita(id, usuario.getId());
        return ResponseEntity.ok().build();
    }

}
