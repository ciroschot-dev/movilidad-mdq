package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TarifaRepository extends JpaRepository<Tarifa, Long>
{
    // Para buscar "TAXI", "UBER" o "DIDI" directamente
    Optional<Tarifa> findByTipoTransporte(String tipoTransporte);
}
