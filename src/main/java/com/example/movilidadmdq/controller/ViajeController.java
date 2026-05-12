package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.repository.UsuarioRepository;
import com.example.movilidadmdq.service.ViajeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/viajes")
@RestController
public class ViajeController
{
    private final ViajeService viajeService;
    private final UsuarioRepository usuarioRepository;

@PostMapping("/calcular")
public ResponseEntity<List<OpcionTransporteResponse>> calcular(@Valid @RequestBody CalculoViajeRequest request, Authentication authentication)
{
    if (authentication == null || authentication.getName() == null)
    {
        return ResponseEntity.status(401).build();
    }
    return usuarioRepository.findByUsername(authentication.getName())
            .map(usuario -> ResponseEntity.ok(viajeService.calcularViaje(
                request.origen(), request.destino(), usuario.getId(),
                request.origenLat(), request.origenLng(),
                request.destinoLat(), request.destinoLng()
            )))
            .orElse(ResponseEntity.status(401).build());
}