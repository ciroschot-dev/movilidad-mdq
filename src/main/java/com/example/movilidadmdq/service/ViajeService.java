package com.example.movilidadmdq.service;

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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ViajeService
{

    // === Configuración ===
    @Value("${taxi.telefono:+542234941010}")
    private String telefonoTaxi;

    //HARDCODEADO
   /* private static final BigDecimal TAXI_BAJADA_DIURNA = BigDecimal.valueOf(2250);
    private static final BigDecimal TAXI_FICHA_DIURNA = BigDecimal.valueOf(150);
    private static final BigDecimal TAXI_BAJADA_NOCTURNA = BigDecimal.valueOf(2700);
    private static final BigDecimal TAXI_FICHA_NOCTURNA = BigDecimal.valueOf(180);
    private static final double METROS_POR_FICHA = 160; */

    //CON BDD
    private final TarifaService tarifaService;

    // === Inyecciones ===
    private final GoogleMapsService googleMapsService;
    private final WeatherService weatherService;
    private final com.example.movilidadmdq.repository.UsuarioRepository usuarioRepository;
    private final com.example.movilidadmdq.repository.ViajeRepository viajeRepository;
    private final UberDeepLinkService uberDeepLinkService;
    private final DidiDeepLinkService didiDeepLinkService;

    public List<OpcionTransporteResponse> calcularViaje(String origen, String destino, Long usuarioId, Double origenLat, Double origenLng, Double destinoLat, Double destinoLng)
    {
        // 🔵 VALORES POR DEFECTO
        double distanciaKm = 5.0;
        int tiempoMin = 15;

        // Usar coordenadas para Google Maps si están disponibles, de lo contrario usar nombres
        String originSearch = (origenLat != null && origenLng != null) ? origenLat + "," + origenLng : normalizarDireccion(origen);
        String destinationSearch = (destinoLat != null && destinoLng != null) ? destinoLat + "," + destinoLng : normalizarDireccion(destino);
        
        LatLng origenCoords = (origenLat != null && origenLng != null) ? new LatLng(origenLat, origenLng) : null;
        LatLng destinoCoords = (destinoLat != null && destinoLng != null) ? new LatLng(destinoLat, destinoLng) : null;

        try
        {
            DistanceMatrix matrix = googleMapsService.obtenerDatosViaje(originSearch, destinationSearch);
            if (esRespuestaValida(matrix))
            {
                DistanceMatrixElement element = matrix.rows[0].elements[0];
                distanciaKm = element.distance.inMeters / 1000.0;
                tiempoMin = (int) Math.ceil(element.duration.inSeconds / 60.0);
                System.out.println("📍 Distancia calculada por Google Maps: " + distanciaKm + " km");
            }
            else
            {
                System.err.println("⚠️ Google Maps no pudo calcular la ruta (usando default 5km). Status: " + 
                    (matrix != null && matrix.rows.length > 0 ? matrix.rows[0].elements[0].status : "NULL"));
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
                construirUber(precioTaxiBase, tiempoMin, origen, origenCoords, destino, destinoCoords, factorClima, distanciaMetros),
                construirDidi(precioTaxiBase, tiempoMin, origen, origenCoords, destino, destinoCoords, factorClima, distanciaMetros)
        );

        return opciones.stream()
                .sorted(Comparator.comparing(OpcionTransporteResponse::precioMin))
                .toList();
    }


    public void guardarViajeConfirmado(com.example.movilidadmdq.dto.ConfirmarViajeRequest request, Long usuarioId)
    {
        try
        {
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

                // Llenar campos viejos para compatibilidad con schema antiguo
                nuevoViaje.setPrecioMinApp(request.precioUberMin());
                nuevoViaje.setPrecioMaxApp(request.precioUberMax());

                viajeRepository.save(nuevoViaje);
                System.out.println("✅ Viaje guardado en el historial para el usuario: " + usuario.getUsername());
            });
        }
        catch (Exception e)
        {
            System.err.println("❌ Error al guardar historial: " + e.getMessage());
        }
    }

    private String normalizarDireccion(String direccion)
    {
        if (direccion == null || direccion.isBlank()) return "Mar del Plata, Argentina";
        if (direccion.toLowerCase().contains("mar del plata")) return direccion;
        return direccion + ", Mar del Plata, Argentina";
    }

    private boolean esRespuestaValida(DistanceMatrix matrix)
    {
        return matrix != null && matrix.rows.length > 0 &&
                matrix.rows[0].elements.length > 0 &&
                matrix.rows[0].elements[0].status.toString().equals("OK");
    }

    // =========================
    // 🚕 TAXI
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
        return (hora >= 22 || hora < 6);
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
            String origen, LatLng origenCoords,
            String destino, LatLng destinoCoords,
            double factorClima,
            long distanciaMetros
    )
    {
        // Uber Base es un 15% menos que el Taxi base
        BigDecimal baseUber = precioBaseTaxi.multiply(BigDecimal.valueOf(0.85));
        
        double fH = obtenerFactorHorario();
        double fD = obtenerFactorDemanda();

        // Factor combinado: Horario * Demanda * Clima (atenuado)
        double factorCombinado = (fH * fD * (1 + (factorClima - 1) * 0.5));

        BigDecimal precioMin = baseUber.multiply(BigDecimal.valueOf(factorCombinado * 0.9));
        BigDecimal precioMax = baseUber.multiply(BigDecimal.valueOf(factorCombinado * 1.2));

        return new OpcionTransporteResponse(
                TipoTransporte.UBER,
                precioMin.setScale(2, RoundingMode.HALF_UP),
                precioMax.setScale(2, RoundingMode.HALF_UP),
                tiempoMin,
                distanciaMetros,
                generarUrlUber(origen, origenCoords, destino, destinoCoords)
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
        // Didi Base es un 20% menos que el Taxi base
        BigDecimal baseDidi = precioBaseTaxi.multiply(BigDecimal.valueOf(0.80));

        double fH = obtenerFactorHorario();
        double fD = obtenerFactorDemanda();
        
        // Moderamos factores
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
    // 📊 FACTORES DINÁMICOS (Moderados)
    // =========================

    private double obtenerFactorHorario()
    {
        int hora = LocalTime.now().getHour();
        // Pico mañana (7-9): 1.15 (antes 1.3)
        if (hora >= 7 && hora <= 9) return 1.15;
        // Pico tarde (17-20): 1.2 (antes 1.4)
        if (hora >= 17 && hora <= 20) return 1.2;
        // Nocturno: 1.1 (antes 1.2)
        if (hora >= 22 || hora < 6) return 1.1;
        return 1.0;
    }

    private double obtenerFactorClima()
    {
        return weatherService.obtenerFactorClima();
    }

    private double obtenerFactorDemanda()
    {
        // Moderamos la variabilidad de la demanda aleatoria
        int autosDisponibles = (int) (Math.random() * 10);
        if (autosDisponibles < 2) return 1.3; // Pico de demanda raro
        if (autosDisponibles < 5) return 1.1; // Demanda moderada
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

