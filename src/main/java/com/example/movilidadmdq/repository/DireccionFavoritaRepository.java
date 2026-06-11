package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.model.DireccionFavorita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DireccionFavoritaRepository extends JpaRepository<DireccionFavorita, Long> {
    List<DireccionFavorita> findByUsuarioId(Long usuarioId);
    Optional<DireccionFavorita> findByUsuarioIdAndDireccion(Long usuarioId, String direccion);
    Optional<DireccionFavorita> findByUsuarioIdAndPlaceId(Long usuarioId, String placeId);
}
