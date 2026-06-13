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

    /**
    Punto de entrada para la actualización de precios del servicio de Taxi.
    Este método actúa como nexo entre la petición HTTP y la lógica de negocio:

           1. @RequestBody: Convierte automáticamente el JSON que envía el Administrador
              en un objeto Java de tipo 'TarifaRequest'.
           2. @Valid: Activa la validación de los campos del DTO (asegura que los precios
              sean positivos y los campos obligatorios estén presentes).
           3. Delegación: El controlador no realiza cálculos ni guarda en la DB; simplemente
              le entrega los datos ya validados al 'tarifaService'.
           4. Respuesta: Retorna el objeto 'Tarifa' actualizado, el cual Spring transformará
              nuevamente a JSON para informar al Admin que la operación fue exitosa.  */

    public Tarifa actualizarTarifaTaxi(@Valid @RequestBody TarifaRequest request)
    {
        return tarifaService.actualizarTarifaTaxi(request);
    }
}
