package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.enums.TipoTransporte;
import com.example.movilidadmdq.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acceso a la base de datos para las tarifas (tablas de precios).
 * <p>
 * Hoy solo se usa la del taxi, pero el diseño deja lugar para otros tipos de
 * transporte sin cambios.
 */
public interface TarifaRepository extends JpaRepository<Tarifa, Long>
{
    /**
     * Trae la configuración de precios de un transporte (ej: TAXI).
     * <p>
     * La consulta el {@code ViajeService} en cada cotización para tomar la
     * bajada de bandera y el valor de la ficha.
     */
    Optional<Tarifa> findByTipoTransporte(TipoTransporte tipoTransporte);
}
