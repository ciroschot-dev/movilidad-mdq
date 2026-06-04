package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.enums.TipoTransporte;
import com.example.movilidadmdq.model.Tarifa;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.LatLng;
import com.google.maps.model.DistanceMatrixElement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

        // --- GUARDAR EN BASE DE DATOS ---
        guardarHistorial(origenFinal, destinoFinal, (long) (distanciaKm * 1000), tiempoMin, precioTaxi, usuarioId);

        List<OpcionTransporteResponse> opciones = List.of(
                construirTaxi(precioTaxi, tiempoMin),
                construirUber(precioTaxi, tiempoMin, request, factorClima),
                construirDidi(precioTaxi, tiempoMin, factorClima)
        );

        // 💸 ordenar por precio más bajo
        return opciones.stream()
                .sorted(Comparator.comparing(OpcionTransporteResponse::precioMin))
                .toList();
    }

    private void guardarHistorial(String origen, String destino, Long distanciaMetros, int tiempoMin, BigDecimal precioTaxi, Long usuarioId)
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
                nuevoViaje.setPrecioMinApp(precioTaxi.multiply(BigDecimal.valueOf(0.85)).setScale(2, RoundingMode.HALF_UP));
                nuevoViaje.setPrecioMaxApp(precioTaxi.multiply(BigDecimal.valueOf(1.2)).setScale(2, RoundingMode.HALF_UP));

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

    // =========================
    // 🚕 TAXI (tarifa real)
    // =========================

    private BigDecimal calcularTaxiBase(double distanciaKm)
    {
        Tarifa tarifa = tarifaService.obtenerTarifaTaxi();
        BigDecimal precioBase = tarifa.getPrecioBase();
        BigDecimal precioPorKm = tarifa.getPrecioPorKm();

        return precioBase.add(precioPorKm.multiply(BigDecimal.valueOf(distanciaKm)));
    }

    private BigDecimal aplicarSargoNocturnoTaxi(BigDecimal precioBase)
    {
        if (esHorarioNocturno()) {
            return precioBase.multiply(BigDecimal.valueOf(1.2)); // +20% oficial
        }
        return precioBase;
    }

    private boolean esHorarioNocturno()
    {
        int hora = LocalTime.now().getHour();
        return hora >= 22 || hora < 6;
    }

    private OpcionTransporteResponse construirTaxi(BigDecimal precioTaxi, int tiempoMin)
    {
        return new OpcionTransporteResponse(
                TipoTransporte.TAXI,
                precioTaxi,
                precioTaxi,
                tiempoMin,
                generarUrlTaxi()
        );
    }

    // =========================
    // 🚗 UBER
    // =========================
    private OpcionTransporteResponse construirUber(BigDecimal precioTaxi, int tiempoMin, CalculoViajeRequest request, double factorClima)
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
                generarUrlUber(request)
        );
    }

    // =========================
    // 🚙 DIDI
    // =========================
    private OpcionTransporteResponse construirDidi(BigDecimal precioTaxi, int tiempoMin, double factorClima)
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
                "https://www.didiglobal.com/"
        );
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

    private String generarUrlUber(String origen, LatLng origenCoords, String destino, LatLng destinoCoords)
    {
        if (origenCoords != null && destinoCoords != null)
        {
            return uberDeepLinkService.generarDeepLink(
                    origen, origenCoords.lat, origenCoords.lng,
                    destino, destinoCoords.lat, destinoCoords.lng
            );
        }

        return "uber://?action=setPickup" +
                "&pickup[formatted_address]=" + encode(origen) +
                "&dropoff[formatted_address]=" + encode(destino);
    }
}

