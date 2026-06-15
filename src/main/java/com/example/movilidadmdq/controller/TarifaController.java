package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.dto.TarifaRequest;
import com.example.movilidadmdq.model.Tarifa;
import com.example.movilidadmdq.service.TarifaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de administración para ajustar las tarifas del taxi.
 * <p>
 * Solo lo usan los administradores para actualizar los precios con los que
 * después se calculan los viajes. El controlador no hace cálculos ni toca la
 * base: recibe el pedido, lo valida y se lo pasa al {@code TarifaService}.
 */
@Tag(name = "Tarifas - Admin", description = "Gestión de tarifas de transporte. Solo accesible por administradores.")
@RestController
@RequestMapping("/admin/tarifas/taxi")
@RequiredArgsConstructor
public class TarifaController
{
    private final TarifaService tarifaService;

    /**
     * Actualiza los valores del sistema de fichas del taxi (solo admin).
     * <p>
     * Recibe la bajada de bandera (día/noche), el valor de cada ficha
     * (día/noche) y los metros que equivalen a una ficha. Admite cambios
     * parciales: el admin manda solo los campos que quiere tocar.
     * <p>
     * Spring convierte el JSON en un {@link TarifaRequest} y {@code @Valid}
     * chequea que los valores sean válidos antes de delegar en el service.
     * Devuelve la tarifa ya actualizada.
     */
    @Operation(summary = "Actualizar tarifa taxi", description = "Actualiza el sistema de fichas del taxi: bajada de bandera y valor de ficha (día y noche) y metros por ficha. Admite actualización parcial. Solo accesible por administradores.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarifa actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Tarifa actualizarTarifaTaxi(@Valid @RequestBody TarifaRequest request)
    {
        return tarifaService.actualizarTarifaTaxi(request);
    }
}
