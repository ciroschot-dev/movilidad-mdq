package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.TarifaRequest;
import com.example.movilidadmdq.enums.TipoTransporte;
import com.example.movilidadmdq.model.Tarifa;
import com.example.movilidadmdq.repository.TarifaRepository;
import org.springframework.stereotype.Service;

@Service
public class TarifaService
{
    private final TarifaRepository tarifaRepository;

    public TarifaService(TarifaRepository tarifaRepository)
    {
        this.tarifaRepository = tarifaRepository;
    }

    public Tarifa obtenerTarifaTaxi()
    {
        return tarifaRepository.findByTipoTransporte(TipoTransporte.TAXI)
                .orElseThrow(() -> new RuntimeException("No se encontro la tarifa del taxi"));
    }

    public Tarifa actualizarTarifaTaxi(TarifaRequest request)
    {
        Tarifa tarifaTaxi = tarifaRepository.findByTipoTransporte(TipoTransporte.TAXI)
                .orElseThrow(() -> new RuntimeException("No se encontro la tarifa de taxi"));

        if (request.getPrecioBase() != null) tarifaTaxi.setPrecioBase(request.getPrecioBase());
        if (request.getPrecioPorKm() != null) tarifaTaxi.setPrecioPorKm(request.getPrecioPorKm());
        if (request.getBajadaBanderaDia() != null) tarifaTaxi.setBajadaBanderaDia(request.getBajadaBanderaDia());
        if (request.getBajadaBanderaNoche() != null) tarifaTaxi.setBajadaBanderaNoche(request.getBajadaBanderaNoche());
        if (request.getValorFichaDia() != null) tarifaTaxi.setValorFichaDia(request.getValorFichaDia());
        if (request.getValorFichaNoche() != null) tarifaTaxi.setValorFichaNoche(request.getValorFichaNoche());
        if (request.getMetrosPorFicha() != null) tarifaTaxi.setMetrosPorFicha(request.getMetrosPorFicha());

        return tarifaRepository.save(tarifaTaxi);
    }
}
