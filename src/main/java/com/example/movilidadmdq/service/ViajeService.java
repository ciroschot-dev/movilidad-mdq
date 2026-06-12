package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.DestinoPopularResponse;
import com.example.movilidadmdq.dto.DireccionFavoritaResponse;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.dto.ViajeHistorialResponse;
import com.example.movilidadmdq.enums.TipoTransporte;
import com.example.movilidadmdq.exception.OperacionNoPermitidaException;
import com.example.movilidadmdq.exception.RecursoNoEncontradoException;
import com.example.movilidadmdq.model.Viaje;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ViajeService
{
    // === Inyecciones ===
    private final GoogleMapsService googleMapsService;
    private final com.example.movilidadmdq.repository.UsuarioRepository usuarioRepository;
    private final com.example.movilidadmdq.repository.ViajeRepository viajeRepository;
    private final CalculadoraTaxiService calculadoraTaxiService;
    private final EstimadorPrecioAppService estimadorPrecioAppService;

    public List<DestinoPopularResponse> obtenerDestinosPopulares(LocalDateTime desde, LocalDateTime hasta, String zona)
    {
        return viajeRepository.findPopularDestinations(desde, hasta, zona, PageRequest.of(0, 10));
    }

    public List<OpcionTransporteResponse> calcularViaje(CalculoViajeRequest request, Long usuarioId)
    {
        String origen = request.origen();
        String destino = request.destino();

        // 🔵 VALORES POR DEFECTO (Simulación / Fallback)
        double distanciaKm = 5.0;
        int tiempoMin = 15;

        // Asegurar que la búsqueda sea en Mar del Plata si no se especificó
        String origenFinal = normalizarDireccion(origen);
        String destinoFinal = normalizarDireccion(destino);

        System.out.println("Solicitando viaje: [" + origenFinal + "] -> [" + destinoFinal + "]");

        // Intento de obtener datos reales de Google Maps
        try
        {
            DistanceMatrix matrix = googleMapsService.obtenerDatosViaje(origenFinal, destinoFinal);
            if (esRespuestaValida(matrix))
            {
                DistanceMatrixElement element = matrix.rows[0].elements[0];

                // Convertir metros a Kilómetros y segundos a Minutos
                distanciaKm = element.distance.inMeters / 1000.0;
                tiempoMin = (int) Math.ceil(element.duration.inSeconds / 60.0);

                System.out.println("Datos REALES obtenidos: " + distanciaKm + "km, " + tiempoMin + "min");
            }
        }
        catch (Exception e)
        {
            System.err.println("Error Google Maps API: " + e.getMessage());
        }

        long distanciaMetros = (long) (distanciaKm * 1000);

        BigDecimal precioTaxi = calculadoraTaxiService.calcularPrecio(distanciaKm);

        OpcionTransporteResponse taxi = construirTaxi(precioTaxi, tiempoMin, distanciaMetros);
        List<OpcionTransporteResponse> apps = estimadorPrecioAppService.estimarOpciones(precioTaxi, tiempoMin, request, distanciaMetros);
        OpcionTransporteResponse uber = apps.get(0);
        OpcionTransporteResponse didi = apps.get(1);

        // --- GUARDAR EN BASE DE DATOS AL FINAL CON PRECIOS REALES ---
        guardarHistorial(origenFinal, destinoFinal, distanciaMetros, tiempoMin,
                taxi.precioMin(), uber.precioMin(), uber.precioMax(),
                didi.precioMin(), didi.precioMax(), usuarioId, request);

        List<OpcionTransporteResponse> opciones = List.of(taxi, uber, didi);

        // 💸 ordenar por precio más bajo
        return opciones.stream()
                .sorted(Comparator.comparing(OpcionTransporteResponse::precioMin))
                .toList();
    }

    private void guardarHistorial(String origen, String destino, Long distanciaMetros, int tiempoMin,
                                  BigDecimal precioTaxi, BigDecimal uberMin, BigDecimal uberMax,
                                  BigDecimal didiMin, BigDecimal didiMax, Long usuarioId, CalculoViajeRequest request)
    {
        if (usuarioId == null) return;

        try
        {
            usuarioRepository.findById(usuarioId).ifPresent(usuario ->
            {
                com.example.movilidadmdq.model.Viaje nuevoViaje = new com.example.movilidadmdq.model.Viaje();
                nuevoViaje.setOrigen(origen);
                nuevoViaje.setDestino(destino);
                nuevoViaje.setDistanciaEnMetros(distanciaMetros);
                nuevoViaje.setTiempoEstimadoMin(tiempoMin);
                nuevoViaje.setPrecioTaxi(precioTaxi);

                nuevoViaje.setPrecioUberMin(uberMin);
                nuevoViaje.setPrecioUberMax(uberMax);
                nuevoViaje.setPrecioDidiMin(didiMin);
                nuevoViaje.setPrecioDidiMax(didiMax);

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

    private String normalizarDireccion(String direccion)
    {
        if (direccion == null || direccion.isBlank()) return "";
        if (direccion.toLowerCase().contains("mar del plata")) return direccion;
        return direccion + ", Mar del Plata, Argentina";
    }

    private boolean esRespuestaValida(DistanceMatrix matrix)
    {
        return matrix != null
                && matrix.rows.length > 0
                && matrix.rows[0].elements.length > 0
                && matrix.rows[0].elements[0].status.toString().equals("OK")
                && matrix.rows[0].elements[0].distance != null
                && matrix.rows[0].elements[0].duration != null;
    }

    private OpcionTransporteResponse construirTaxi(BigDecimal precioTaxi, int tiempoMin, long distanciaMetros)
    {
        return new OpcionTransporteResponse(
                TipoTransporte.TAXI,
                precioTaxi,
                precioTaxi,
                tiempoMin,
                distanciaMetros,
                "tel:+5402234941010"
        );
    }

    @Transactional
    public void toggleFavorito(Long viajeId, Long usuarioId)
    {

        // Caso 1: el viaje no existe en la DB -> 404.
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Viaje no encontrado"));

        // Caso 2: el viaje existe pero es de otro usuario -> 403.
        //
        // Nota: devolver 403 (distinto del 404) revela al cliente que el viaje
        // existe. En contextos con datos sensibles eso seria una fuga de info y
        // se preferiria responder 404 tambien aca. En esta API priorizamos
        // claridad semantica: el JWT del usuario es valido, lo que falla es la
        // autorizacion a nivel recurso. "Se quien sos, pero esto no es tuyo".
        if (!viaje.getUsuario().getId().equals(usuarioId))
        {
            throw new OperacionNoPermitidaException("No tienes permiso para modificar este viaje");
        }

        viaje.setFavorito(!viaje.isFavorito());

        viajeRepository.save(viaje);
    }

    public List<Viaje> obtenerFavoritos(Long usuarioId)
    {
        return viajeRepository.findByUsuarioIdAndFavoritoTrue(usuarioId);
    }

    public List<DireccionFavoritaResponse> obtenerDireccionesFavoritas(Long usuarioId)
    {
        List<Viaje> favoritos = obtenerFavoritos(usuarioId);

        // Usamos un mapa para evitar duplicados basado en la dirección o el placeId
        java.util.Map<String, DireccionFavoritaResponse> direcciones = new java.util.LinkedHashMap<>();

        for (Viaje v : favoritos)
        {
            // Procesar Origen
            if (v.getOrigen() != null)
            {
                String key = v.getOrigenPlaceId() != null ? v.getOrigenPlaceId() : v.getOrigen();
                direcciones.putIfAbsent(key, new DireccionFavoritaResponse(
                        v.getOrigen(),
                        v.getOrigenPlaceId(),
                        v.getOrigenLat(),
                        v.getOrigenLng()
                ));
            }

            // Procesar Destino
            if (v.getDestino() != null)
            {
                String key = v.getDestinoPlaceId() != null ? v.getDestinoPlaceId() : v.getDestino();
                direcciones.putIfAbsent(key, new DireccionFavoritaResponse(
                        v.getDestino(),
                        v.getDestinoPlaceId(),
                        v.getDestinoLat(),
                        v.getDestinoLng()
                ));
            }
        }

        return new java.util.ArrayList<>(direcciones.values());
    }

    public ViajeHistorialResponse toResponse(Viaje viaje)
    {
        return new ViajeHistorialResponse(
                viaje.getId(),
                viaje.getOrigen(),
                viaje.getDestino(),
                viaje.getDistanciaEnMetros(),
                viaje.getTiempoEstimadoMin(),
                viaje.getPrecioTaxi(),
                viaje.getPrecioUberMin(),
                viaje.getPrecioUberMax(),
                viaje.getPrecioDidiMin(),
                viaje.getPrecioDidiMax(),
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
