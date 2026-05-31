package com.example.movilidadmdq.service;

import com.example.movilidadmdq.enums.TipoTransporte;
import com.example.movilidadmdq.model.Tarifa;
import com.example.movilidadmdq.repository.TarifaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service

public class TarifaService {
    private final TarifaRepository tarifaRepository;

    public TarifaService(TarifaRepository tarifaRepository) {
        this.tarifaRepository = tarifaRepository;
    }

    public Tarifa obtenerTarifaTaxi(){
        return tarifaRepository.findByTipoTransporte(TipoTransporte.TAXI)
                .orElseThrow(()-> new RuntimeException("No se encontro la tarifa del taxi"));
    }
    public Tarifa actualizarTarifaTaxi(BigDecimal precioBase, BigDecimal precioPorKm){
        Tarifa tarifaTaxi = tarifaRepository.findByTipoTransporte(TipoTransporte.TAXI)
                .orElseThrow(() -> new RuntimeException("No se encontro la tarifa de taxi"));

        tarifaTaxi.setPrecioBase(precioBase);
        tarifaTaxi.setPrecioPorKm(precioPorKm);

        return tarifaRepository.save(tarifaTaxi);
    }
}
