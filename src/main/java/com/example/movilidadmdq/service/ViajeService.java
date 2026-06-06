package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.ConfirmarViajeRequest;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.enums.TipoTransporte;
import com.example.movilidadmdq.model.Tarifa;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.LatLng;
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
@Transactional
public class ViajeService
{
    // === Configuración ===
    @Value("${taxi.telefono:+542234941010}")
    private String telefonoTaxi;

    // === Inyecciones ===
    private final GoogleMapsService googleMapsService;
    private final WeatherService weatherService;
    private final TarifaService tarifaService;
    private final com.example.movilidadmdq.repository.UsuarioRepository usuarioRepository;
    private final com.example.movilidadmdq.repository.ViajeRepository viajeRepository;
    private final UberDeepLinkService uberDeepLinkService;
    private final DidiDeepLinkService didiDeepLinkService;

    public List<OpcionTransporteResponse> calcularViaje(CalculoViajeRequest request, Long usuarioId)
    {
        // 🔵 VALORES POR DEFECTO
        double distanciaKm = 5.0;
        int tiempoMin = 15;

        // Usar coordenadas para Google Maps si están disponibles, de lo contrario usar nombres
        String originSearch = (request.origenLat() != null && request.origenLng() != null) ? request.origenLat() + "," + request.origenLng() : normalizarDireccion(request.origen());
        String destinationSearch = (request.destinoLat() != null && request.destinoLng() != null) ? request.destinoLat() + "," + request.destinoLng() : normalizarDireccion(request.destino());
        
        LatLng origenCoords = (request.origenLat() != null && request.origenLng() != null) ? new LatLng(request.origenLat(), request.origenLng()) : null;
        LatLng destinoCoords = (request.destinoLat() != null && request.destinoLng() != null) ? new LatLng(request.destinoLat(), request.destinoLng()) : null;

        System.out.println("Solicitando viaje: [" + originSearch + "] -> [" + destinationSearch + "]");

        // Intento de obtener datos reales de Google Maps
        try
        {
            DistanceMatrix matrix = googleMapsService.obtenerDatosViaje(originSearch, destinationSearch);
            if (esRespuestaValida(matrix))
            {
                DistanceMatrixElement element = matrix.rows[0].elements[0];

                // Convertir metros a Kilómetros y segundos a Minutos
                distanciaKm = element.distance.inMeters / 1000.0;
                tiempoMin = (int) Math.ceil(element.duration.inSeconds / 60.0);
                System.out.println("✅ Datos REALES obtenidos: " + distanciaKm + "km, " + tiempoMin + "min");
            }
        }
        catch (Exception e)
        {
            System.err.println("❌ Error Google Maps API: " + e.getMessage() + " (usando default 5km)");
        }

        // Calculamos el PRECIO BASE del taxi (sin nocturno) para usarlo como base comparativa
        BigDecimal precioTaxiBase = calcularTaxiBase(distanciaKm);
        
        BigDecimal precioTaxiFinal = aplicarSargoNocturnoTaxi(precioTaxiBase);
        double factorClima = obtenerFactorClima();
        long distanciaMetros = (long) (distanciaKm * 1000);

        System.out.println("📊 Factores: Clima=" + factorClima + " | Distancia=" + distanciaMetros + "m");

        List<OpcionTransporteResponse> opciones = List.of(
                construirTaxi(precioTaxiFinal, tiempoMin, distanciaMetros),
                construirUber(precioTaxiBase, tiempoMin, request, factorClima, distanciaMetros),
                construirDidi(precioTaxiBase, tiempoMin, request.origen(), origenCoords, request.destino(), destinoCoords, factorClima, distanciaMetros)
        );

        // 💸 ordenar por precio más bajo
        return opciones.stream()
                .sorted(Comparator.comparing(OpcionTransporteResponse::precioMin))
                .toList();
    }

    public void guardarViajeConfirmado(ConfirmarViajeRequest request, Long usuarioId)
    {
        if (usuarioId == null) return;

        usuarioRepository.findById(usuarioId).ifPresent(usuario ->
        {
            com.example.movilidadmdq.model.Viaje nuevoViaje = new com.example.movilidadmdq.model.Viaje();
            nuevoViaje.setOrigen(request.origen());
            nuevoViaje.setDestino(request.destino());
            nuevoViaje.setDistanciaEnMetros(request.distanciaEnMetros());
            nuevoViaje.setTiempoEstimadoMin(request.tiempoEstimadoMin());
            nuevoViaje.setPrecioTaxi(request.precioTaxi());
            nuevoViaje.setPrecioUberMin(request.precioUberMin());
            nuevoViaje.setPrecioUberMax(request.precioUberMax());
            nuevoViaje.setPrecioDidiMin(request.precioDidiMin());
            nuevoViaje.setPrecioDidiMax(request.precioDidiMax());
            nuevoViaje.setTipoElegido(request.tipoElegido());
            nuevoViaje.setUsuario(usuario);

            // Llenar campos viejos con la opcion elegida para bases anteriores.
            switch (request.tipoElegido()) {
                case TAXI -> {
                    nuevoViaje.setPrecioMinApp(request.precioTaxi());
                    nuevoViaje.setPrecioMaxApp(request.precioTaxi());
                }
                case UBER -> {
                    nuevoViaje.setPrecioMinApp(request.precioUberMin());
                    nuevoViaje.setPrecioMaxApp(request.precioUberMax());
                }
                case DIDI -> {
                    nuevoViaje.setPrecioMinApp(request.precioDidiMin());
                    nuevoViaje.setPrecioMaxApp(request.precioDidiMax());
                }
            }

            viajeRepository.save(nuevoViaje);
            System.out.println("Viaje guardado en el historial para el usuario: " + usuario.getUsername());
        });
    }

    private String normalizarDireccion(String direccion)
    {
        if (direccion == null || direccion.isBlank()) return "Mar del Plata, Argentina";
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

    private OpcionTransporteResponse construirTaxi(BigDecimal precioTaxi, int tiempoMin, long distanciaMetros)
    {
        return new OpcionTransporteResponse(
                TipoTransporte.TAXI,
                precioTaxi.setScale(2, RoundingMode.HALF_UP),
                precioTaxi.setScale(2, RoundingMode.HALF_UP),
                tiempoMin,
                distanciaMetros,
                "tel:" + telefonoTaxi
        );
    }

    // =========================
    // 🚗 UBER
    // =========================

    private OpcionTransporteResponse construirUber(
            BigDecimal precioBaseTaxi, int tiempoMin,
            CalculoViajeRequest request,
            double factorClima,
            long distanciaMetros
    )
    {
        BigDecimal baseUber = precioBaseTaxi.multiply(BigDecimal.valueOf(0.85));
        
        double fH = obtenerFactorHorario();
        double fD = obtenerFactorDemanda();

        double factorCombinado = (fH * fD * (1 + (factorClima - 1) * 0.5));

        BigDecimal precioMin = baseUber.multiply(BigDecimal.valueOf(factorCombinado * 0.9));
        BigDecimal precioMax = baseUber.multiply(BigDecimal.valueOf(factorCombinado * 1.2));

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

    private OpcionTransporteResponse construirDidi(
            BigDecimal precioBaseTaxi, int tiempoMin,
            String origen, LatLng origenCoords,
            String destino, LatLng destinoCoords,
            double factorClima,
            long distanciaMetros
    )
    {
        BigDecimal baseDidi = precioBaseTaxi.multiply(BigDecimal.valueOf(0.80));

        double fH = obtenerFactorHorario();
        double fD = obtenerFactorDemanda();
        
        double factorCombinado = (fH * (1 + (fD - 1) * 0.5) * (1 + (factorClima - 1) * 0.3));

        BigDecimal precioMin = baseDidi.multiply(BigDecimal.valueOf(factorCombinado * 0.85));
        BigDecimal precioMax = baseDidi.multiply(BigDecimal.valueOf(factorCombinado * 1.15));

        return new OpcionTransporteResponse(
                TipoTransporte.DIDI,
                precioMin.setScale(2, RoundingMode.HALF_UP),
                precioMax.setScale(2, RoundingMode.HALF_UP),
                tiempoMin,
                distanciaMetros,
                generarUrlDidi(origen, origenCoords, destino, destinoCoords)
        );
    }

    private String generarUrlDidi(String origen, LatLng origenCoords, String destino, LatLng destinoCoords)
    {
        if (origenCoords != null && destinoCoords != null)
        {
            return didiDeepLinkService.generarDeepLink(
                    origen, origenCoords.lat, origenCoords.lng,
                    destino, destinoCoords.lat, destinoCoords.lng
            );
        }
        return "https://www.didiglobal.com/";
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

    private String generarUrlUber(CalculoViajeRequest request)
    {
        if (request.origenLat() == null || request.origenLng() == null ||
            request.destinoLat() == null || request.destinoLng() == null) {
            return "https://m.uber.com/ul/?action=setPickup"
                    + "&pickup[formatted_address]=" + encode(request.origen())
                    + "&dropoff[formatted_address]=" + encode(request.destino());
        }

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

    private String valorOTexto(String value, String fallback)
    {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String escapeJson(String value)
    {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
