package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.DireccionFavoritaResponse;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.dto.ViajeHistorialResponse;
import com.example.movilidadmdq.enums.TipoTransporte;
import com.example.movilidadmdq.model.Viaje;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ViajeService
{
    // === Inyecciones ===
    private final GoogleMapsService googleMapsService;
    private final WeatherService weatherService;
    private final TarifaService tarifaService;
    private final UberDeepLinkService uberDeepLinkService;
    private final DidiDeepLinkService didiDeepLinkService;
    private final com.example.movilidadmdq.repository.UsuarioRepository usuarioRepository;
    private final com.example.movilidadmdq.repository.ViajeRepository viajeRepository;
    private final com.example.movilidadmdq.repository.DireccionFavoritaRepository direccionFavoritaRepository;

    @Value("${taxi.telefono:+542234941010}")
    private String telefonoTaxi;

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

        BigDecimal precioTaxi = calcularTaxi(distanciaKm);
        double factorClima = obtenerFactorClima();

        // --- GUARDAR EN BASE DE DATOS ---
        long distanciaMetros = (long) (distanciaKm * 1000);
        guardarHistorial(origenFinal, destinoFinal, distanciaMetros, tiempoMin, precioTaxi, usuarioId, request);

        List<OpcionTransporteResponse> opciones = List.of(
                construirTaxi(precioTaxi, tiempoMin, distanciaMetros),
                construirUber(precioTaxi, tiempoMin, request, factorClima, distanciaMetros),
                construirDidi(precioTaxi, tiempoMin, factorClima, distanciaMetros)
        );

        // 💸 ordenar por precio más bajo
        return opciones.stream()
                .sorted(Comparator.comparing(OpcionTransporteResponse::precioMin))
                .toList();
    }

    private void guardarHistorial(String origen, String destino, Long distanciaMetros, int tiempoMin, BigDecimal precioTaxi, Long usuarioId, CalculoViajeRequest request)
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

                // Valores estimados para historial
                BigDecimal uberMin = precioTaxi.multiply(BigDecimal.valueOf(0.85)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal uberMax = precioTaxi.multiply(BigDecimal.valueOf(1.2)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal didiMin = precioTaxi.multiply(BigDecimal.valueOf(0.75)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal didiMax = precioTaxi.multiply(BigDecimal.valueOf(1.1)).setScale(2, RoundingMode.HALF_UP);

                nuevoViaje.setPrecioUberMin(uberMin);
                nuevoViaje.setPrecioUberMax(uberMax);
                nuevoViaje.setPrecioDidiMin(didiMin);
                nuevoViaje.setPrecioDidiMax(didiMax);

                // Compatibilidad
                nuevoViaje.setPrecioMinApp(uberMin);
                nuevoViaje.setPrecioMaxApp(uberMax);

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
            e.printStackTrace();
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

    // =========================
    // 🚕 TAXI (tarifa real)
    // =========================
    private BigDecimal calcularTaxi(double distanciaKm)
    {
        com.example.movilidadmdq.model.Tarifa tarifa = tarifaService.obtenerTarifaTaxi();
        BigDecimal precioBase = tarifa.getPrecioBase();
        BigDecimal precioPorKm = tarifa.getPrecioPorKm();

        return precioBase.add(precioPorKm.multiply(BigDecimal.valueOf(distanciaKm)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private OpcionTransporteResponse construirTaxi(BigDecimal precioTaxi, int tiempoMin, long distanciaMetros)
    {
        return new OpcionTransporteResponse(
                TipoTransporte.TAXI,
                precioTaxi,
                precioTaxi,
                tiempoMin,
                distanciaMetros,
                generarUrlTaxi()
        );
    }

    // =========================
    // 🚗 UBER
    // =========================
    private OpcionTransporteResponse construirUber(BigDecimal precioTaxi, int tiempoMin, CalculoViajeRequest request, double factorClima, long distanciaMetros)
    {
        BigDecimal base = precioTaxi.multiply(BigDecimal.valueOf(0.85)); // base más barato que taxi

        double factorHorario = obtenerFactorHorario();
        double factorDemanda = obtenerFactorDemanda();

        BigDecimal precioMin = base.multiply(BigDecimal.valueOf(factorHorario * factorClima));
        BigDecimal precioMax = base.multiply(BigDecimal.valueOf(factorHorario * factorClima * factorDemanda));

        return new OpcionTransporteResponse(
                TipoTransporte.UBER,
                precioMin.setScale(2, RoundingMode.HALF_UP),
                precioMax.setScale(2, RoundingMode.HALF_UP),
                tiempoMin,
                distanciaMetros,
                generarUrlUber(request)
        );
    }

    // =========================
    // 🚙 DIDI
    // =========================
    private OpcionTransporteResponse construirDidi(BigDecimal precioTaxi, int tiempoMin, double factorClima, long distanciaMetros)
    {
        BigDecimal base = precioTaxi.multiply(BigDecimal.valueOf(0.75));

        double factorHorario = obtenerFactorHorario();
        double factorDemanda = obtenerFactorDemanda();

        BigDecimal precioMin = base.multiply(BigDecimal.valueOf(factorHorario));
        BigDecimal precioMax = base.multiply(BigDecimal.valueOf(factorHorario * factorClima * factorDemanda));

        return new OpcionTransporteResponse(
                TipoTransporte.DIDI,
                precioMin.setScale(2, RoundingMode.HALF_UP),
                precioMax.setScale(2, RoundingMode.HALF_UP),
                tiempoMin,
                distanciaMetros,
                generarUrlDidi(null) // Pasamos null si no hay request específico para didi aún
        );
    }

    // =========================
    // 🔗 URLs
    // =========================
    private String generarUrlTaxi()
    {
        return "tel:" + telefonoTaxi;
    }

    private String generarUrlUber(CalculoViajeRequest request)
    {
        if (request.origenLat() != null && request.origenLng() != null &&
            request.destinoLat() != null && request.destinoLng() != null) {
            return uberDeepLinkService.generarDeepLink(
                request.origen(), request.origenLat(), request.origenLng(),
                request.destino(), request.destinoLat(), request.destinoLng()
            );
        }
        return "https://m.uber.com/";
    }

    private String generarUrlDidi(CalculoViajeRequest request)
    {
        if (request != null && request.origenLat() != null && request.origenLng() != null &&
            request.destinoLat() != null && request.destinoLng() != null) {
            return didiDeepLinkService.generarDeepLink(
                request.origen(), request.origenLat(), request.origenLng(),
                request.destino(), request.destinoLat(), request.destinoLng()
            );
        }
        return "https://www.didiglobal.com/";
    }

    private String valorOTexto(String value, String fallback)
    {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String escapeJson(String value)
    {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String encode(String value)
    {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // =========================
    // 📊 FACTORES DINÁMICOS
    // =========================
    private double obtenerFactorHorario()
    {
        int hora = LocalTime.now().getHour();

        if (hora >= 7 && hora <= 9) return 1.3; // hora pico mañana
        if (hora >= 17 && hora <= 20) return 1.4; // hora pico tarde
        if (hora >= 22 || hora < 6) return 1.2; // noche

        return 1.0;
    }

    private double obtenerFactorClima()
    {
        return weatherService.obtenerFactorClima();
    }

    private double obtenerFactorDemanda()
    {
        int autosDisponibles = (int) (Math.random() * 10);

        if (autosDisponibles < 3) return 1.5;
        if (autosDisponibles < 6) return 1.2;

        return 1.0;
    }

    @Transactional
    public void toggleFavorito(Long viajeId, Long usuarioId) {

        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));

        if (!viaje.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para modificar este viaje");
        }

        boolean nuevoEstado = !viaje.isFavorito();
        viaje.setFavorito(nuevoEstado);
        viajeRepository.save(viaje);

        if (nuevoEstado) {
            // Al marcar como favorito, aseguramos que las direcciones estén en la tabla de favoritos
            syncDireccionFavorita(viaje.getOrigen(), viaje.getOrigenPlaceId(), viaje.getOrigenLat(), viaje.getOrigenLng(), usuarioId);
            syncDireccionFavorita(viaje.getDestino(), viaje.getDestinoPlaceId(), viaje.getDestinoLat(), viaje.getDestinoLng(), usuarioId);
        }
    }

    private void syncDireccionFavorita(String direccion, String placeId, Double lat, Double lng, Long usuarioId) {
        if (direccion == null) return;

        java.util.Optional<com.example.movilidadmdq.model.DireccionFavorita> existing = (placeId != null && !placeId.isBlank())
                ? direccionFavoritaRepository.findByUsuarioIdAndPlaceId(usuarioId, placeId)
                : direccionFavoritaRepository.findByUsuarioIdAndDireccion(usuarioId, direccion);

        if (existing.isEmpty()) {
            com.example.movilidadmdq.model.DireccionFavorita df = new com.example.movilidadmdq.model.DireccionFavorita();
            df.setDireccion(direccion);
            df.setPlaceId(placeId);
            df.setLat(lat);
            df.setLng(lng);
            df.setUsuario(usuarioRepository.getReferenceById(usuarioId));
            direccionFavoritaRepository.save(df);
        }
    }

    public List<Viaje> obtenerFavoritos(Long usuarioId) {
        return viajeRepository.findByUsuarioIdAndFavoritoTrue(usuarioId);
    }

    @Transactional
    public List<DireccionFavoritaResponse> obtenerDireccionesFavoritas(Long usuarioId) {
        List<com.example.movilidadmdq.model.DireccionFavorita> saved = direccionFavoritaRepository.findByUsuarioId(usuarioId);

        if (saved.isEmpty()) {
            // Migración inicial: si no hay nada en la tabla nueva, buscar en los viajes favoritos
            List<Viaje> favoritos = obtenerFavoritos(usuarioId);
            for (Viaje v : favoritos) {
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
    public void renombrarDireccionFavorita(Long id, String nuevoNombre, Long usuarioId) {
        com.example.movilidadmdq.model.DireccionFavorita df = direccionFavoritaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirección favorita no encontrada"));

        if (!df.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para modificar este favorito");
        }

        df.setNombre(nuevoNombre);
        direccionFavoritaRepository.save(df);
    }

    @Transactional
    public void eliminarDireccionFavorita(Long id, Long usuarioId) {
        com.example.movilidadmdq.model.DireccionFavorita df = direccionFavoritaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirección favorita no encontrada"));

        if (!df.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para eliminar este favorito");
        }

        direccionFavoritaRepository.delete(df);
    }

    public ViajeHistorialResponse toResponse(Viaje viaje) {
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
