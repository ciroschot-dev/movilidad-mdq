package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.model.Viaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ViajeRepository extends JpaRepository<Viaje, Long>
{
    List<Viaje> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);
}
