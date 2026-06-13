package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.DireccionFavoritaResponse;
import com.example.movilidadmdq.exception.OperacionNoPermitidaException;
import com.example.movilidadmdq.exception.RecursoNoEncontradoException;
import com.example.movilidadmdq.model.DireccionFavorita;
import com.example.movilidadmdq.model.Viaje;
import com.example.movilidadmdq.repository.DireccionFavoritaRepository;
import com.example.movilidadmdq.repository.UsuarioRepository;
import com.example.movilidadmdq.repository.ViajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Gestiona los viajes favoritos del usuario y sus direcciones favoritas.
 *
 * <p>Cuando un viaje se marca como favorito, su origen y destino se guardan
 * además como direcciones favoritas reutilizables, que el usuario puede
 * renombrar (Casa, Trabajo) o eliminar.
 */
@Service
@RequiredArgsConstructor
public class FavoritoService
{
    private final ViajeRepository viajeRepository;
    private final DireccionFavoritaRepository direccionFavoritaRepository;
    private final UsuarioRepository usuarioRepository;

    /** Marca o desmarca un viaje como favorito. Falla si no existe (404) o es de otro usuario (403). */
    @Transactional
    public void toggleFavorito(Long viajeId, Long usuarioId)
    {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Viaje no encontrado"));

        if (!viaje.getUsuario().getId().equals(usuarioId))
        {
            throw new OperacionNoPermitidaException("No tienes permiso para modificar este viaje");
        }

        boolean nuevoEstado = !viaje.isFavorito();
        viaje.setFavorito(nuevoEstado);
        viajeRepository.save(viaje);

        // Al marcar favorito, guardamos sus direcciones para reutilizarlas.
        if (nuevoEstado)
        {
            syncDireccionFavorita(viaje.getOrigen(), viaje.getOrigenPlaceId(), viaje.getOrigenLat(), viaje.getOrigenLng(), usuarioId);
            syncDireccionFavorita(viaje.getDestino(), viaje.getDestinoPlaceId(), viaje.getDestinoLat(), viaje.getDestinoLng(), usuarioId);
        }
    }

    public List<Viaje> obtenerFavoritos(Long usuarioId)
    {
        return viajeRepository.findByUsuarioIdAndFavoritoTrue(usuarioId);
    }

    @Transactional
    public List<DireccionFavoritaResponse> obtenerDireccionesFavoritas(Long usuarioId)
    {
        List<DireccionFavorita> saved = direccionFavoritaRepository.findByUsuarioId(usuarioId);

        // Backfill: usuarios que ya tenían viajes favoritos antes de esta feature
        // no tienen direcciones guardadas; las derivamos en la primera lectura.
        if (saved.isEmpty())
        {
            for (Viaje v : obtenerFavoritos(usuarioId))
            {
                syncDireccionFavorita(v.getOrigen(), v.getOrigenPlaceId(), v.getOrigenLat(), v.getOrigenLng(), usuarioId);
                syncDireccionFavorita(v.getDestino(), v.getDestinoPlaceId(), v.getDestinoLat(), v.getDestinoLng(), usuarioId);
            }
            saved = direccionFavoritaRepository.findByUsuarioId(usuarioId);
        }

        return saved.stream()
                .map(df -> new DireccionFavoritaResponse(
                        df.getId(),
                        df.getNombre(),
                        df.getDireccion(),
                        df.getPlaceId(),
                        df.getLat(),
                        df.getLng()))
                .toList();
    }

    @Transactional
    public void renombrarDireccionFavorita(Long id, String nuevoNombre, Long usuarioId)
    {
        DireccionFavorita df = buscarPropia(id, usuarioId);
        df.setNombre(nuevoNombre);
        direccionFavoritaRepository.save(df);
    }

    @Transactional
    public void eliminarDireccionFavorita(Long id, Long usuarioId)
    {
        DireccionFavorita df = buscarPropia(id, usuarioId);
        direccionFavoritaRepository.delete(df);
    }

    // Busca una dirección favorita validando que sea del usuario.
    // 404 si no existe, 403 si es de otro (traducidos por el GlobalExceptionHandler).
    private DireccionFavorita buscarPropia(Long id, Long usuarioId)
    {
        DireccionFavorita df = direccionFavoritaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dirección favorita no encontrada"));

        if (!df.getUsuario().getId().equals(usuarioId))
        {
            throw new OperacionNoPermitidaException("No tienes permiso para modificar este favorito");
        }
        return df;
    }

    // Guarda la dirección como favorita si el usuario no la tenía ya (dedup por placeId o texto).
    private void syncDireccionFavorita(String direccion, String placeId, Double lat, Double lng, Long usuarioId)
    {
        if (direccion == null) return;

        Optional<DireccionFavorita> existing = (placeId != null && !placeId.isBlank())
                ? direccionFavoritaRepository.findByUsuarioIdAndPlaceId(usuarioId, placeId)
                : direccionFavoritaRepository.findByUsuarioIdAndDireccion(usuarioId, direccion);

        if (existing.isEmpty())
        {
            DireccionFavorita df = new DireccionFavorita();
            df.setDireccion(direccion);
            df.setPlaceId(placeId);
            df.setLat(lat);
            df.setLng(lng);
            df.setUsuario(usuarioRepository.getReferenceById(usuarioId));
            direccionFavoritaRepository.save(df);
        }
    }
}
