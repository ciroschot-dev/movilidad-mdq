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

/* 
   INTERFAZ: ViajeRepository
   
   Este componente es responsable de toda la persistencia del historial de viajes.
   - Hereda de JpaRepository para las operaciones CRUD estándar.
   - Hereda de JpaSpecificationExecutor para permitir filtros dinámicos y complejos 
     en la pantalla de auditoría del Administrador.
*/
public interface ViajeRepository extends JpaRepository<Viaje, Long>, JpaSpecificationExecutor<Viaje>
{
    /* 
       MÉTODO: findByUsuarioIdOrderByFechaHoraDesc
       Recupera la lista cronológica de viajes de un usuario.
       El sufijo 'OrderByFechaHoraDesc' asegura que los viajes más recientes 
       aparezcan al principio de la lista en el frontend.
    */
    List<Viaje> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);

    /* 
       MÉTODO: findByUsuarioIdAndFavoritoTrue
       Filtra únicamente aquellos viajes que el usuario marcó con la "estrella" de favorito.
    */
    List<Viaje> findByUsuarioIdAndFavoritoTrue(Long usuarioId);

    /* 
       MÉTODO: findPopularDestinations (ESTADÍSTICAS ADMIN)
       Utiliza JPQL (Java Persistence Query Language) para realizar una consulta de agregación.
       
       LÓGICA:
       1. Filtra por rango de fechas (:desde y :hasta) si se proporcionan.
       2. Permite buscar por una 'zona' o palabra clave dentro del nombre del destino.
       3. Agrupa por nombre de destino y cuenta cuántas veces se repite cada uno.
       4. Instancia automáticamente objetos 'DestinoPopularResponse' para enviar al frontend.
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
