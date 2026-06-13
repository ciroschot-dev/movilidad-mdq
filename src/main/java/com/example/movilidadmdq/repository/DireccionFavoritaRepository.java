package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.model.DireccionFavorita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/* 
   INTERFAZ: DireccionFavoritaRepository
   
   Esta interfaz gestiona los puntos geográficos que los usuarios deciden guardar 
   con un alias (ej: "Casa", "Trabajo").
   
   ESTRATEGIA DE DATOS:
   Permite desacoplar las direcciones del historial de viajes, facilitando que 
   el usuario gestione sus lugares frecuentes de forma independiente y eficiente.
*/
@Repository
public interface DireccionFavoritaRepository extends JpaRepository<DireccionFavorita, Long> {
    
    /* 
       MÉTODO: findByUsuarioId
       Recupera la lista completa de direcciones guardadas por un usuario específico.
    */
    List<DireccionFavorita> findByUsuarioId(Long usuarioId);

    /* 
       MÉTODO: findByUsuarioIdAndDireccion
       Busca si una dirección textual ya fue guardada por el usuario. 
       Se utiliza principalmente para evitar duplicados cuando el usuario intenta 
       guardar una ubicación nueva.
    */
    Optional<DireccionFavorita> findByUsuarioIdAndDireccion(Long usuarioId, String direccion);

    /* 
       MÉTODO: findByUsuarioIdAndPlaceId
       Busca un favorito basado en el ID único de Google Maps. 
       Es el método más preciso de verificación, ya que el PlaceID es invariable 
       aunque cambie el nombre textual de la calle.
    */
    Optional<DireccionFavorita> findByUsuarioIdAndPlaceId(Long usuarioId, String placeId);
}
