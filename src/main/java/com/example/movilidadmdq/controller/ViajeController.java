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
import lombok.extern.slf4j.Slf4j;
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

/**
 * Motor funcional de la app: calcular viajes y gestionar favoritos.
 * <p>
 * Expone el cálculo de rutas con comparación de precios entre taxi, Uber y Didi,
 * el marcado de viajes favoritos y la administración de las direcciones
 * favoritas de cada usuario.
 */
@Tag(name = "Viajes", description = "Cálculo y comparación de opciones de transporte.")
@RequiredArgsConstructor
@RequestMapping("/viajes")
@RestController
@Slf4j
public class ViajeController
{
    private final ViajeService viajeService;
    private final HistorialViajeService historialViajeService;
    private final FavoritoService favoritoService;

    /**
     * Lista y filtra todos los viajes del sistema, para control y monitoreo (solo admin).
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

    /** Estadística de los lugares más consultados por los ciudadanos (solo admin). */
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

    /**
     * Núcleo de la app: recibe un viaje y devuelve la comparativa de precios y
     * tiempos de llegada de cada opción de transporte.
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
        log.info("""

        🗺️ INICIANDO FLUJO CONSULTAR VIAJE:
        -> 1. ViajeController.calcular: Recibe request y autentica.
        -> 2. ViajeService.calcularViaje: Orquesta el flujo completo.
        -> 3. GoogleMapsService.obtenerDatosViaje: API Externa (Distancia/Tiempo).
        -> 4. WeatherService.obtenerFactorClima: API Externa (Precios dinámicos).
        -> 5. CalculadoraTaxiService / EstimadorApp: Cálculo matemático.
        -> 6. HistorialViajeService.guardar: Persistencia en MySQL.
        -> 7. Retorno: Lista de opciones ordenadas por precio.
        """);

        return ResponseEntity.ok(viajeService.calcularViaje(request, usuario.getId()));
    }

    /** Marca o desmarca un viaje del historial como favorito. */
    @PutMapping("/{viajeId}/favorito")
    public ResponseEntity<Void> toggleFavorito(
            @PathVariable Long viajeId,
            @AuthenticationPrincipal Usuario usuario)
    {
        favoritoService.toggleFavorito(viajeId, usuario.getId());
        return ResponseEntity.ok().build();
    }

    /** Devuelve todos los viajes que el usuario marcó como favoritos. */
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

    /** Devuelve los lugares guardados por el usuario (ej: "Trabajo", "Casa de mi abuela"). */
    @Operation(summary = "Obtener direcciones favoritas", description = "Devuelve una lista única de todas las direcciones (orígenes y destinos) marcadas como favoritas.")
    @GetMapping("/direcciones-favoritas")
    public ResponseEntity<List<DireccionFavoritaResponse>> obtenerDireccionesFavoritas(
            @AuthenticationPrincipal Usuario usuario
    )
    {
        return ResponseEntity.ok(favoritoService.obtenerDireccionesFavoritas(usuario.getId()));
    }

    /** Cambia el alias de una de las direcciones guardadas del usuario. */
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

    /** Elimina una dirección de la lista de favoritos del usuario. */
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
