package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.service.ViajeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/viajes")
@RestController
@CrossOrigin(origins = "*")
public class ViajeController
{
    private final ViajeService viajeService;

    @PostMapping("/calcular")
    public List<OpcionTransporteResponse> calcular(@Valid @RequestBody CalculoViajeRequest request)
    {
        return viajeService.calcularViaje(
            request.origen(), request.destino(), request.usuarioId(),
            request.origenLat(), request.origenLng(),
            request.destinoLat(), request.destinoLng()
        );
    }
}
