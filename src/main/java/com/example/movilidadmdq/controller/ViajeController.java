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

@Tag(name = "Viajes", description = "Cálculo y comparación de opciones de transporte.")
@RequiredArgsConstructor
@RequestMapping("/viajes")
@RestController
public class ViajeController
{
    private final ViajeService viajeService;
    private final HistorialViajeService historialViajeService;
    private final FavoritoService favoritoService;

    @Operation(summary = "Auditoría de viajes (ADMIN)", description = "Devuelve una página de viajes realizados por todos los usuarios con filtros avanzados.")
    @GetMapping("/admin/auditoria")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ViajeHistorialResponse>> getAuditoria(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String origen,
            @RequestParam(required = false) String destino,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Boolean favorito,
            @PageableDefault(size = 10, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable)
    {
        return ResponseEntity.ok(historialViajeService.obtenerAuditoriaViajes(username, origen, destino, desde, hasta, favorito, pageable));
    }

    @Operation(summary = "Obtener destinos populares (ADMIN)", description = "Devuelve los destinos más buscados con filtros opcionales.")
    @GetMapping("/admin/destinos-populares")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DestinoPopularResponse>> getDestinosPopulares(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) String zona)
    {
        return ResponseEntity.ok(historialViajeService.obtenerDestinosPopulares(desde, hasta, zona));
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
            @AuthenticationPrincipal Usuario usuario
    )
    {
        return ResponseEntity.ok(viajeService.calcularViaje(request, usuario.getId()));
    }

    // Metodo para marcar viaje fav
    @PutMapping("/{viajeId}/favorito")
    public ResponseEntity<Void> toggleFavorito(
            @PathVariable Long viajeId,
            @AuthenticationPrincipal Usuario usuario)
    {
        favoritoService.toggleFavorito(viajeId, usuario.getId());
        return ResponseEntity.ok().build();
    }

    // Metodo para obtener favs
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

    @Operation(summary = "Obtener direcciones favoritas", description = "Devuelve una lista única de todas las direcciones (orígenes y destinos) marcadas como favoritas.")
    @GetMapping("/direcciones-favoritas")
    public ResponseEntity<List<DireccionFavoritaResponse>> obtenerDireccionesFavoritas(
            @AuthenticationPrincipal Usuario usuario
    )
    {
        return ResponseEntity.ok(favoritoService.obtenerDireccionesFavoritas(usuario.getId()));
    }

    @Operation(summary = "Renombrar dirección favorita", description = "Permite asignar un nombre personalizado (alias) a una dirección favorita.")
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

    @Operation(summary = "Eliminar dirección favorita", description = "Quita una dirección de la lista de favoritos.")
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
