package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.dto.TarifaRequest;
import com.example.movilidadmdq.model.Tarifa;
import com.example.movilidadmdq.service.TarifaService;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Tarifas - Admin", description = "Gestión de tarifas de transporte. Solo accesible por administradores.")
@RestController
@RequestMapping("/admin/tarifas/taxi")
@RequiredArgsConstructor
public class TarifaController
{
    private final TarifaService tarifaService;

    @Operation(summary = "Actualizar tarifa taxi", description = "Actualiza el precio base y el precio por km del servicio taxi. Solo accesible por administradores.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarifa actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")

    public Tarifa actualizarTarifaTaxi(@RequestBody TarifaRequest request)
    {
        return tarifaService.actualizarTarifaTaxi(request);
    }
}
