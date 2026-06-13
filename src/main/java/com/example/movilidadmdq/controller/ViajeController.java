package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.dto.ActualizarDireccionFavoritaRequest;
import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.DestinoPopularResponse;
import com.example.movilidadmdq.dto.DireccionFavoritaResponse;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.dto.ViajeHistorialResponse;
import com.example.movilidadmdq.model.Viaje;
import com.example.movilidadmdq.repository.UsuarioRepository;
import com.example.movilidadmdq.service.FavoritoService;
import com.example.movilidadmdq.service.ViajeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Viajes", description = "Cálculo y comparación de opciones de transporte.")
@RequiredArgsConstructor
@RequestMapping("/viajes")
@RestController
public class ViajeController
{
    private final ViajeService viajeService;
    private final FavoritoService favoritoService;
    private final UsuarioRepository usuarioRepository;

    @Operation(summary = "Obtener destinos más buscados (ADMIN)", description = "Devuelve una lista de destinos populares con filtros por fecha y zona. Solo accesible por administradores.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida con éxito"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    @GetMapping("/admin/destinos-populares")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DestinoPopularResponse>> getDestinosPopulares(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) String zona)
    {
        return ResponseEntity.ok(viajeService.obtenerDestinosPopulares(desde, hasta, zona));
    }

    @Operation(
            summary = "Calcular viaje",
            description = "Obtiene y calcula el viaje solicitado por el usuario. Si se envian datos de Google Places, genera deep links enriquecidos para abrir apps externas con origen y destino precargados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "El viaje se ha calculado con exito"),
            @ApiResponse(responseCode = "400", description = "El calculo del viaje fallo"),
            @ApiResponse(responseCode = "401", description = "Los datos ingresados son incorrectos")
    })
    @PostMapping("/calcular")
    public ResponseEntity<List<OpcionTransporteResponse>> calcular(
            @Valid @RequestBody CalculoViajeRequest request,
            Authentication authentication
    )
    {
        if (authentication == null || authentication.getName() == null)
        {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .map(usuario -> ResponseEntity.ok(viajeService.calcularViaje(request, usuario.getId())))
                .orElse(ResponseEntity.status(401).build());
    }

    // Metodo para marcar vviaje fav
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

    // Meotodo para obtener favs
    @GetMapping("/favoritos")
    public ResponseEntity<List<ViajeHistorialResponse>> obtenerFavoritos(
            Authentication authentication
    )
    {
        if (authentication == null || authentication.getName() == null)
        {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .map(usuario ->
                        ResponseEntity.ok(
                                favoritoService.obtenerFavoritos(usuario.getId()).stream()
                                        .map(viajeService::toResponse)
                                        .toList()
                        ))
                .orElse(ResponseEntity.status(401).build());
    }

    @Operation(summary = "Obtener direcciones favoritas", description = "Devuelve una lista única de todas las direcciones (orígenes y destinos) marcadas como favoritas.")
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

    @Operation(summary = "Renombrar dirección favorita", description = "Permite asignar un nombre personalizado (alias) a una dirección favorita.")
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

    @Operation(summary = "Eliminar dirección favorita", description = "Quita una dirección de la lista de favoritos.")
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
