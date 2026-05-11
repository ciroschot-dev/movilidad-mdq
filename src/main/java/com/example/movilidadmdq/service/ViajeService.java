package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.enums.TipoTransporte;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.LatLng;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ViajeService {

    // === Configuración ===
    @Value("${taxi.telefono:+5492233126129}")
    private String telefonoTaxi;

    private static final BigDecimal TAXI_BAJADA_DIURNA = BigDecimal.valueOf(2250);
    private static final BigDecimal TAXI_FICHA_DIURNA = BigDecimal.valueOf(150);
    private static final BigDecimal TAXI_BAJADA_NOCTURNA = BigDecimal.valueOf(2700);
    private static final BigDecimal TAXI_FICHA_NOCTURNA = BigDecimal.valueOf(180);
    private static final double METROS_POR_FICHA = 160;

    // === Inyecciones ===
    private final GoogleMapsService googleMapsService;
    private final WeatherService weatherService;
    private final com.example.movilidadmdq.repository.UsuarioRepository usuarioRepository;
    private final com.example.movilidadmdq.repository.ViajeRepository viajeRepository;
    private final UberDeepLinkService uberDeepLinkService;
    private final DidiDeepLinkService didiDeepLinkService;

    public List<OpcionTransporteResponse> calcularViaje(
        String origen, String destino, Long usuarioId,
        Double origenLat, Double origenLng, Double destinoLat, Double destinoLng
    ) {
        // 🔵 VALORES POR DEFECTO (Simulación / Fallback)
        double distanciaKm = 5.0;
        int tiempoMin = 15;

        // Asegurar que la búsqueda sea en Mar del Plata si no se especificó
        String origenFinal = normalizarDireccion(origen);
        String destinoFinal = normalizarDireccion(destino);

        System.out.println("Solicitando viaje: [" + origenFinal + "] -> [" + destinoFinal + "]");

        try {
            DistanceMatrix matrix = googleMapsService.obtenerDatosViaje(origenFinal, destinoFinal);
            if (esRespuestaValida(matrix)) {
                DistanceMatrixElement element = matrix.rows[0].elements[0];
                distanciaKm = element.distance.inMeters / 1000.0;
                tiempoMin = (int) Math.ceil(element.duration.inSeconds / 60.0);
                System.out.println("✅ Datos REALES obtenidos: " + distanciaKm + "km, " + tiempoMin + "min");
            }
        } catch (Exception e) {
            System.err.println("❌ Error Google Maps API: " + e.getMessage());
        }

        BigDecimal precioTaxi = calcularTaxi(distanciaKm);

        // Coordenadas recibidas desde el frontend (autocomplete) para deep links
        LatLng origenCoords = (origenLat != null && origenLng != null) ? new LatLng(origenLat, origenLng) : null;
        LatLng destinoCoords = (destinoLat != null && destinoLng != null) ? new LatLng(destinoLat, destinoLng) : null;

        // --- GUARDAR EN BASE DE DATOS ---
        guardarHistorial(origenFinal, destinoFinal, (long)(distanciaKm * 1000), tiempoMin, precioTaxi, usuarioId);

        List<OpcionTransporteResponse> opciones = List.of(
                construirTaxi(precioTaxi, tiempoMin),
                construirUber(precioTaxi, tiempoMin, origen, origenCoords, destino, destinoCoords),
                construirDidi(precioTaxi, tiempoMin, origen, origenCoords, destino, destinoCoords)
        );

        return opciones.stream()
                .sorted(Comparator.comparing(OpcionTransporteResponse::precioMin))
                .toList();
    }

    private void guardarHistorial(String origen, String destino, Long distanciaMetros, int tiempoMin, BigDecimal precioTaxi, Long usuarioId) {
        if (usuarioId == null) return;
        try {
            usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
                com.example.movilidadmdq.model.Viaje nuevoViaje = new com.example.movilidadmdq.model.Viaje();
                nuevoViaje.setOrigen(origen);
                nuevoViaje.setDestino(destino);
                nuevoViaje.setDistanciaEnMetros(distanciaMetros);
                nuevoViaje.setTiempoEstimadoMin(tiempoMin);
                nuevoViaje.setPrecioTaxi(precioTaxi);
                
                // Valores estimados para historial
                nuevoViaje.setPrecioMinApp(precioTaxi.multiply(BigDecimal.valueOf(0.85)).setScale(2, RoundingMode.HALF_UP));
                nuevoViaje.setPrecioMaxApp(precioTaxi.multiply(BigDecimal.valueOf(1.2)).setScale(2, RoundingMode.HALF_UP));
                
                nuevoViaje.setUsuario(usuario);
                
                viajeRepository.save(nuevoViaje);
                System.out.println("💾 Viaje guardado automáticamente en AWS para el usuario: " + usuario.getUsername());
            });
        } catch (Exception e) {
            System.err.println("❌ Error al guardar historial: " + e.getMessage());
        }
    }

    private String normalizarDireccion(String direccion) {
        if (direccion.toLowerCase().contains("mar del plata")) return direccion;
        return direccion + ", Mar del Plata, Argentina";
    }

    private boolean esRespuestaValida(DistanceMatrix matrix) {
        return matrix != null && matrix.rows.length > 0 && 
               matrix.rows[0].elements.length > 0 && 
               matrix.rows[0].elements[0].status.toString().equals("OK");
    }

    // =========================
    // 🚕 TAXI
    // =========================

    private BigDecimal calcularTaxi(double distanciaKm) {
        boolean esNocturno = esHorarioNocturno();
        BigDecimal bajada = esNocturno ? TAXI_BAJADA_NOCTURNA : TAXI_BAJADA_DIURNA;
        BigDecimal valorFicha = esNocturno ? TAXI_FICHA_NOCTURNA : TAXI_FICHA_DIURNA;

        double distanciaMetros = distanciaKm * 1000;
        int fichas = (int) Math.ceil(distanciaMetros / METROS_POR_FICHA);
        
        BigDecimal precioFichas = valorFicha.multiply(BigDecimal.valueOf(fichas));
        return bajada.add(precioFichas);
    }

    private boolean esHorarioNocturno() {
        int hora = LocalTime.now().getHour();
        return (hora >= 22 || hora < 6);
    }

    private OpcionTransporteResponse construirTaxi(BigDecimal precioTaxi, int tiempoMin) {
        return new OpcionTransporteResponse(
                TipoTransporte.TAXI,
                precioTaxi,
                precioTaxi,
                tiempoMin,
                "tel:" + telefonoTaxi
        );
    }

    // =========================
    // 🚗 UBER
    // =========================

    private OpcionTransporteResponse construirUber(BigDecimal precioTaxi, int tiempoMin, String origen, LatLng origenCoords, String destino, LatLng destinoCoords) {
        BigDecimal base = precioTaxi.multiply(BigDecimal.valueOf(0.85));
        
        double fH = obtenerFactorHorario();
        double fC = obtenerFactorClima();
        double fD = obtenerFactorDemanda();

        BigDecimal precioMin = base.multiply(BigDecimal.valueOf(fH * fC));
        BigDecimal precioMax = base.multiply(BigDecimal.valueOf(fH * fC * fD));

        return new OpcionTransporteResponse(
                TipoTransporte.UBER,
                precioMin.setScale(2, RoundingMode.HALF_UP),
                precioMax.setScale(2, RoundingMode.HALF_UP),
                tiempoMin,
                generarUrlUber(origen, origenCoords, destino, destinoCoords)
        );
    }

    // =========================
    // 🚙 DIDI
    // =========================

    private OpcionTransporteResponse construirDidi(BigDecimal precioTaxi, int tiempoMin, String origen, LatLng origenCoords, String destino, LatLng destinoCoords) {
        BigDecimal base = precioTaxi.multiply(BigDecimal.valueOf(0.75));

        double fH = obtenerFactorHorario();
        double fC = obtenerFactorClima();
        double fD = obtenerFactorDemanda();

        BigDecimal precioMin = base.multiply(BigDecimal.valueOf(fH));
        BigDecimal precioMax = base.multiply(BigDecimal.valueOf(fH * fC * fD));

        return new OpcionTransporteResponse(
                TipoTransporte.DIDI,
                precioMin.setScale(2, RoundingMode.HALF_UP),
                precioMax.setScale(2, RoundingMode.HALF_UP),
                tiempoMin,
                generarUrlDidi(origen, origenCoords, destino, destinoCoords)
        );
    }

    private String generarUrlUber(String origen, LatLng origenCoords, String destino, LatLng destinoCoords) {
        if (origenCoords != null && destinoCoords != null) {
            return uberDeepLinkService.generarDeepLink(
                    origen, origenCoords.lat, origenCoords.lng,
                    destino, destinoCoords.lat, destinoCoords.lng
            );
        }
        return "uber://?action=setPickup" +
                "&pickup[formatted_address]=" + encode(origen) +
                "&dropoff[formatted_address]=" + encode(destino);
    }

    private String generarUrlDidi(String origen, LatLng origenCoords, String destino, LatLng destinoCoords) {
        if (origenCoords != null && destinoCoords != null) {
            return didiDeepLinkService.generarDeepLink(
                    origen, origenCoords.lat, origenCoords.lng,
                    destino, destinoCoords.lat, destinoCoords.lng
            );
        }
        return "https://www.didiglobal.com/";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // =========================
    // 📊 FACTORES DINÁMICOS
    // =========================

    private double obtenerFactorHorario() {
        int hora = LocalTime.now().getHour();
        if (hora >= 7 && hora <= 9) return 1.3;
        if (hora >= 17 && hora <= 20) return 1.4;
        if (hora >= 22 || hora < 6) return 1.2;
        return 1.0;
    }

    private double obtenerFactorClima() {
        return weatherService.obtenerFactorClima();
    }

    private double obtenerFactorDemanda() {
        // Simulación de demanda basada en aleatoriedad (Pendiente: Integración con API real si existe)
        int autosDisponibles = (int) (Math.random() * 10);
        if (autosDisponibles < 3) return 1.5;
        if (autosDisponibles < 6) return 1.2;
        return 1.0;
    }
}


