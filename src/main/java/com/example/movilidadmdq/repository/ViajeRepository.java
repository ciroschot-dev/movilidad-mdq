package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.dto.DestinoPopularResponse;
import com.example.movilidadmdq.model.Viaje;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ViajeRepository extends JpaRepository<Viaje, Long>
{
    List<Viaje> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);

    List<Viaje> findByUsuarioIdAndFavoritoTrue(Long usuarioId);

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
