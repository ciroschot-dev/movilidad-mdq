package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.DestinoPopularResponse;
import com.example.movilidadmdq.dto.ViajeHistorialResponse;
import com.example.movilidadmdq.model.Viaje;
import com.example.movilidadmdq.repository.UsuarioRepository;
import com.example.movilidadmdq.repository.ViajeRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
     * Devuelve una página de viajes con filtros avanzados para auditoría.
     */
    public Page<ViajeHistorialResponse> obtenerAuditoriaViajes(
            String username,
            String origen,
            String destino,
            LocalDateTime desde,
            LocalDateTime hasta,
            Boolean favorito,
            Pageable pageable)
    {
        Specification<Viaje> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (username != null && !username.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("usuario").get("username")),
                        "%" + username.toLowerCase() + "%"
                ));
            }

            if (origen != null && !origen.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("origen")),
                        "%" + origen.toLowerCase() + "%"
                ));
            }

            if (destino != null && !destino.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("destino")),
                        "%" + destino.toLowerCase() + "%"
                ));
            }

            if (desde != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fechaHora"), desde));
            }

            if (hasta != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fechaHora"), hasta));
            }

            if (favorito != null) {
                predicates.add(criteriaBuilder.equal(root.get("favorito"), favorito));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return viajeRepository.findAll(spec, pageable).map(this::toResponse);
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
                viaje.getUsuario() != null ? viaje.getUsuario().getUsername() : null,
                viaje.getOrigenPlaceId(),
                viaje.getOrigenLat(),
                viaje.getOrigenLng(),
                viaje.getDestinoPlaceId(),
                viaje.getDestinoLat(),
                viaje.getDestinoLng()
        );
    }
}
