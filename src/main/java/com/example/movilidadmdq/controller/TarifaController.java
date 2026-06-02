package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.dto.TarifaRequest;
import com.example.movilidadmdq.model.Tarifa;
import com.example.movilidadmdq.service.TarifaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/tarifas/taxi")
@RequiredArgsConstructor
public class TarifaController {
    private final TarifaService tarifaService;

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Tarifa actualizarTarifaTaxi(@RequestBody TarifaRequest request){
        return tarifaService.actualizarTarifaTaxi(request.getPrecioBase(), request.getPrecioPorKm());
    }
}
