package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.dto.ViajeHistorialResponse;
import com.example.movilidadmdq.model.Viaje;
import com.example.movilidadmdq.repository.UsuarioRepository;
import com.example.movilidadmdq.service.ViajeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Viajes", description = "Cálculo y comparación de opciones de transporte.")
@RequiredArgsConstructor
@RequestMapping("/viajes")
@RestController
public class ViajeController
{
    private final ViajeService viajeService;
    private final UsuarioRepository usuarioRepository;

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
        if (authentication == null || authentication.getName() == null) {
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
            Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .map(usuario -> {
                    viajeService.toggleFavorito(viajeId, usuario.getId());
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.status(401).build());
    }
    // Meotodo para obtener favs
    @GetMapping("/favoritos")
    public ResponseEntity<List<ViajeHistorialResponse>> obtenerFavoritos(
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        return usuarioRepository.findByUsername(authentication.getName())
                .map(usuario ->
                        ResponseEntity.ok(
                                viajeService.obtenerFavoritos(usuario.getId()).stream()
                                        .map(viajeService::toResponse)
                                        .toList()
                        ))
                .orElse(ResponseEntity.status(401).build());
    }

}
