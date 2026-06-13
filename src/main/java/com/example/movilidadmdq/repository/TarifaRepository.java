package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.enums.TipoTransporte;
import com.example.movilidadmdq.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/* 
   INTERFAZ: TarifaRepository
   
   Gestiona la persistencia de las tablas de precios del sistema.
   Aunque actualmente solo manejamos tarifas de TAXI, el diseño permite 
   escalar a otros tipos de transporte fácilmente.
*/
public interface TarifaRepository extends JpaRepository<Tarifa, Long>
{
    /* 
       MÉTODO: findByTipoTransporte
       Busca la configuración de precios para un servicio específico (ej: TAXI).
       Es el método que consulta el 'ViajeService' cada vez que un usuario 
       pide una cotización para obtener la bajada de bandera y el valor de la ficha.
    */
    Optional<Tarifa> findByTipoTransporte(TipoTransporte tipoTransporte);
}
