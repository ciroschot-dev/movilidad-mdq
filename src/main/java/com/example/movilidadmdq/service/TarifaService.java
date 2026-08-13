package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.TarifaRequest;
import com.example.movilidadmdq.enums.TipoTransporte;
import com.example.movilidadmdq.exception.RecursoNoEncontradoException;
import com.example.movilidadmdq.model.Tarifa;
import com.example.movilidadmdq.repository.TarifaRepository;
import org.springframework.stereotype.Service;

/**
 * Gestiona la configuración de precios del taxi de Mar del Plata.
 * <p>
 * Recupera los valores vigentes para que el {@code ViajeService} pueda cotizar,
 * y aplica las actualizaciones del administrador permitiendo cambios parciales
 * (si solo manda un valor, el resto queda intacto).
 */
@Service
public class TarifaService
{
    private final TarifaRepository tarifaRepository;

    public TarifaService(TarifaRepository tarifaRepository)
    {
        this.tarifaRepository = tarifaRepository;
    }

    /**
     * Trae la tarifa vigente del taxi. Si no hay ninguna cargada, lanza 404
     * ({@code RecursoNoEncontradoException}).
     */
    public Tarifa obtenerTarifaTaxi()
    {
        return tarifaRepository.findByTipoTransporte(TipoTransporte.TAXI)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro la tarifa del taxi"));
    }

    /**
     * Aplica los cambios de tarifa que manda el administrador.
     * <p>
     * Solo pisa los campos que vienen distintos de null, así el admin puede
     * tocar un único valor (ej: la ficha diurna) sin reenviar todos los demás.
     */
    public Tarifa actualizarTarifaTaxi(TarifaRequest request)
    {
        Tarifa tarifaTaxi = tarifaRepository.findByTipoTransporte(TipoTransporte.TAXI)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro la tarifa de taxi"));

        // Verificación campo a campo para permitir actualizaciones parciales.
        if (request.getBajadaBanderaDia() != null) tarifaTaxi.setBajadaBanderaDia(request.getBajadaBanderaDia());
        if (request.getBajadaBanderaNoche() != null) tarifaTaxi.setBajadaBanderaNoche(request.getBajadaBanderaNoche());
        if (request.getValorFichaDia() != null) tarifaTaxi.setValorFichaDia(request.getValorFichaDia());
        if (request.getValorFichaNoche() != null) tarifaTaxi.setValorFichaNoche(request.getValorFichaNoche());
        if (request.getMetrosPorFicha() != null) tarifaTaxi.setMetrosPorFicha(request.getMetrosPorFicha());

        // Persiste los cambios actualizados en la base de datos PostgreSQL.
        return tarifaRepository.save(tarifaTaxi);
    }
}
