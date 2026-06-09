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
    private final com.example.movilidadmdq.repository.UsuarioRepository usuarioRepository;
    private final com.example.movilidadmdq.repository.ViajeRepository viajeRepository;

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

        OpcionTransporteResponse taxi = construirTaxi(precioTaxi, tiempoMin, (long) (distanciaKm * 1000));
        OpcionTransporteResponse uber = construirUber(precioTaxi, tiempoMin, request, factorClima, (long) (distanciaKm * 1000));
        OpcionTransporteResponse didi = construirDidi(precioTaxi, tiempoMin, factorClima, (long) (distanciaKm * 1000));

        // --- GUARDAR EN BASE DE DATOS AL FINAL CON PRECIOS REALES ---
        long distanciaMetros = (long) (distanciaKm * 1000);
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
        boolean esNocturno = esHorarioNocturno();

        BigDecimal bajadaBandera = esNocturno ? BigDecimal.valueOf(2700) : BigDecimal.valueOf(2250);
        BigDecimal valorFicha = esNocturno ? BigDecimal.valueOf(180) : BigDecimal.valueOf(150);

        double metrosPorFicha = 160;
        double distanciaMetros = distanciaKm * 1000;

        // calcular fichas
        int fichas = (int) Math.ceil(distanciaMetros / metrosPorFicha);

        // calcular precio por fichas
        BigDecimal precioFichas = valorFicha.multiply(BigDecimal.valueOf(fichas));

        // precio final: bajada de bandera + fichas calculadas
        return bajadaBandera.add(precioFichas);
    }

    private boolean esHorarioNocturno()
    {
        int hora = LocalTime.now().getHour();
        return hora >= 22 || hora < 6;
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
                generarUrlDidi()
        );
    }

    // =========================
    // 🔗 URLs
    // =========================
    private String generarUrlTaxi()
    {
        return "tel:+5492233126129"; // num de Ciro para pruebas. Despues cambiar al de TAXI
    }

    private String generarUrlUber(CalculoViajeRequest request)
    {
        // Fallback: si el frontend no envía coordenadas/place data, usar el deep link simple.
        if (
                request.origenLat() == null ||
                        request.origenLng() == null ||
                        request.destinoLat() == null ||
                        request.destinoLng() == null
        ) {
            return "https://m.uber.com/ul/?action=setPickup"
                    + "&pickup[formatted_address]=" + encode(request.origen())
                    + "&dropoff[formatted_address]=" + encode(request.destino());
        }

        // Formato usado por m.uber.com/go/drop para precargar origen y destino.
        String pickupJson = """
                {
                  "addressLine1": "%s",
                  "addressLine2": "%s",
                  "id": "%s",
                  "source": "SEARCH",
                  "latitude": %s,
                  "longitude": %s,
                  "provider": "google_places"
                }
                """.formatted(
                escapeJson(valorOTexto(request.origenAddressLine1(), request.origen())),
                escapeJson(valorOTexto(request.origenAddressLine2(), request.origen())),
                escapeJson(valorOTexto(request.origenPlaceId(), "")),
                request.origenLat(),
                request.origenLng()
        );

        String dropJson = """
                {
                  "addressLine1": "%s",
                  "addressLine2": "%s",
                  "id": "%s",
                  "source": "SEARCH",
                  "latitude": %s,
                  "longitude": %s,
                  "provider": "google_places"
                }
                """.formatted(
                escapeJson(valorOTexto(request.destinoAddressLine1(), request.destino())),
                escapeJson(valorOTexto(request.destinoAddressLine2(), request.destino())),
                escapeJson(valorOTexto(request.destinoPlaceId(), "")),
                request.destinoLat(),
                request.destinoLng()
        );

        return "https://m.uber.com/go/drop"
                + "?pickup=" + encode(pickupJson)
                + "&drop%5B0%5D=" + encode(dropJson);
    }

    private String generarUrlDidi()
    {
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

        viaje.setFavorito(!viaje.isFavorito());

        viajeRepository.save(viaje);
    }
    public List<Viaje> obtenerFavoritos(Long usuarioId) {
        return viajeRepository.findByUsuarioIdAndFavoritoTrue(usuarioId);
    }

    public List<DireccionFavoritaResponse> obtenerDireccionesFavoritas(Long usuarioId) {
        List<Viaje> favoritos = obtenerFavoritos(usuarioId);
        
        // Usamos un mapa para evitar duplicados basado en la dirección o el placeId
        java.util.Map<String, DireccionFavoritaResponse> direcciones = new java.util.LinkedHashMap<>();

        for (Viaje v : favoritos) {
            // Procesar Origen
            if (v.getOrigen() != null) {
                String key = v.getOrigenPlaceId() != null ? v.getOrigenPlaceId() : v.getOrigen();
                direcciones.putIfAbsent(key, new DireccionFavoritaResponse(
                        v.getOrigen(),
                        v.getOrigenPlaceId(),
                        v.getOrigenLat(),
                        v.getOrigenLng()
                ));
            }

            // Procesar Destino
            if (v.getDestino() != null) {
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
