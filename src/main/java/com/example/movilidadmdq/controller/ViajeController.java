package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.service.ViajeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.Arrays.stream;


@RequiredArgsConstructor
@RequestMapping("/viajes")
@RestController

public class ViajeController
{
    private final ViajeService viajeService;

    @PostMapping("/calcular")

    public List<OpcionTransporteResponse> calcular(@Valid @RequestBody CalculoViajeRequest request)
    {
        return viajeService.calcularViaje(request.getOrigen(), request.getDestino());
    }
}
