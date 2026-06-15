package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.dto.DestinoPopularResponse;
import com.example.movilidadmdq.model.Viaje;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a la base de datos para el historial de viajes.
 * <p>
 * Hereda de {@link JpaRepository} las operaciones básicas y de
 * {@link JpaSpecificationExecutor} la capacidad de armar filtros dinámicos, que
 * usa la pantalla de auditoría del administrador.
 */
public interface ViajeRepository extends JpaRepository<Viaje, Long>, JpaSpecificationExecutor<Viaje>
{
    /** Viajes de un usuario, del más nuevo al más viejo (para mostrar el historial). */
    List<Viaje> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);

    /** Solo los viajes que el usuario marcó como favoritos. */
    List<Viaje> findByUsuarioIdAndFavoritoTrue(Long usuarioId);

    /**
     * Ranking de destinos más consultados (estadística de admin).
     * <p>
     * Es una consulta de agregación: filtra por rango de fechas y por zona si se
     * pasan, agrupa por destino, cuenta cuántas veces aparece cada uno y arma
     * directamente los {@link DestinoPopularResponse} ordenados de mayor a menor.
     */
    @Query("SELECT new com.example.movilidadmdq.dto.DestinoPopularResponse(v.destino, COUNT(v)) " +
           "FROM Viaje v " +
           "WHERE (:desde IS NULL OR v.fechaHora >= :desde) " +
           "AND (:hasta IS NULL OR v.fechaHora <= :hasta) " +
           "AND (:zona IS NULL OR :zona = '' OR LOWER(v.destino) LIKE LOWER(CONCAT('%', :zona, '%'))) " +
           "GROUP BY v.destino " +
           "ORDER BY COUNT(v) DESC")
    List<DestinoPopularResponse> findPopularDestinations(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("zona") String zona,
            Pageable pageable);
}
