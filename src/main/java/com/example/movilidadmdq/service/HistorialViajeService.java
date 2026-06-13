package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.DestinoPopularResponse;
import com.example.movilidadmdq.dto.ViajeHistorialResponse;
import com.example.movilidadmdq.model.Viaje;
import com.example.movilidadmdq.repository.UsuarioRepository;
import com.example.movilidadmdq.repository.ViajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Persiste y consulta el registro de viajes: guarda cada viaje calculado en el
 * historial del usuario, consulta los destinos más buscados (panel admin) y
 * mapea la entidad {@link Viaje} al DTO que se devuelve al front.
 */
@Service
@RequiredArgsConstructor
public class HistorialViajeService
{
    private final UsuarioRepository usuarioRepository;
    private final ViajeRepository viajeRepository;

    /** Devuelve los 10 destinos más buscados, con filtros opcionales por fecha y zona. */
    public List<DestinoPopularResponse> obtenerDestinosPopulares(LocalDateTime desde, LocalDateTime hasta, String zona)
    {
        return viajeRepository.findPopularDestinations(desde, hasta, zona, PageRequest.of(0, 10));
    }

    /**
     * Guarda el viaje calculado en el historial del usuario. Si no hay usuario
     * logueado (usuarioId null) no guarda nada. Cualquier error al persistir se
     * traga: el historial es secundario y no debe romper el cálculo del viaje.
     */
    public void guardar(String origen, String destino, Long distanciaMetros, int tiempoMin,
                        BigDecimal precioTaxi, BigDecimal precioUber, BigDecimal precioDidi,
                        Long usuarioId, CalculoViajeRequest request)
    {
        if (usuarioId == null) return;

        try
        {
            usuarioRepository.findById(usuarioId).ifPresent(usuario ->
            {
                Viaje nuevoViaje = new Viaje();
                nuevoViaje.setOrigen(origen);
                nuevoViaje.setDestino(destino);
                nuevoViaje.setDistanciaEnMetros(distanciaMetros);
                nuevoViaje.setTiempoEstimadoMin(tiempoMin);
                nuevoViaje.setPrecioTaxi(precioTaxi);
                nuevoViaje.setPrecioUber(precioUber);
                nuevoViaje.setPrecioDidi(precioDidi);

                // Guardar coordenadas y Place IDs para optimización futura
                nuevoViaje.setOrigenPlaceId(request.origenPlaceId());
                nuevoViaje.setOrigenLat(request.origenLat());
                nuevoViaje.setOrigenLng(request.origenLng());
                nuevoViaje.setDestinoPlaceId(request.destinoPlaceId());
                nuevoViaje.setDestinoLat(request.destinoLat());
                nuevoViaje.setDestinoLng(request.destinoLng());

                nuevoViaje.setUsuario(usuario);

                viajeRepository.save(nuevoViaje);
                System.out.println("Viaje guardado automaticamente en AWS para el usuario: " + usuario.getUsername());
            });
        }
        catch (Exception e)
        {
            System.err.println("Error al guardar historial: " + e.getMessage());
        }
    }

    /** Convierte una entidad Viaje en el DTO que se devuelve al front. */
    public ViajeHistorialResponse toResponse(Viaje viaje)
    {
        return new ViajeHistorialResponse(
                viaje.getId(),
                viaje.getOrigen(),
                viaje.getDestino(),
                viaje.getDistanciaEnMetros(),
                viaje.getTiempoEstimadoMin(),
                viaje.getPrecioTaxi(),
                viaje.getPrecioUber(),
                viaje.getPrecioDidi(),
                viaje.getTipoElegido() != null ? viaje.getTipoElegido().name() : null,
                viaje.getFechaHora(),
                viaje.isFavorito(),
                viaje.getOrigenPlaceId(),
                viaje.getOrigenLat(),
                viaje.getOrigenLng(),
                viaje.getDestinoPlaceId(),
                viaje.getDestinoLat(),
                viaje.getDestinoLng()
        );
    }
}
