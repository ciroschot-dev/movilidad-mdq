package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.enums.TipoTransporte;
import com.example.movilidadmdq.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TarifaRepository extends JpaRepository<Tarifa, Long>
{
    Optional<Tarifa> findByTipoTransporte(TipoTransporte tipoTransporte);
}
