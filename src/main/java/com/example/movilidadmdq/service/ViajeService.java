package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.enums.TipoTransporte;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Orquesta el cálculo de un viaje: pide la distancia a Google Maps, delega el
 * precio del taxi y la estimación de las apps en sus calculadores, ordena las
 * opciones de más barata a más cara y delega el guardado en el historial.
 *
 * <p>No hace cuentas de precio ni habla con la base directamente: solo coordina
 * a los servicios que sí lo hacen.
 */
@Service
@RequiredArgsConstructor
public class ViajeService
{
    // === Inyecciones ===
    private final GoogleMapsService googleMapsService;
    private final WeatherService weatherService;
    private final CalculadoraTaxiService calculadoraTaxiService;
    private final EstimadorPrecioAppService estimadorPrecioAppService;
    private final HistorialViajeService historialViajeService;

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

        long distanciaMetros = (long) (distanciaKm * 1000);

        BigDecimal precioTaxi = calculadoraTaxiService.calcularPrecio(distanciaKm);

        // El orquestador junta las señales externas (clima) y se las pasa a los calculadores.
        double factorClima = weatherService.obtenerFactorClima();

        OpcionTransporteResponse taxi = construirTaxi(precioTaxi, tiempoMin, distanciaMetros);
        OpcionTransporteResponse uber = estimadorPrecioAppService.estimarUber(precioTaxi, tiempoMin, factorClima, distanciaMetros, request);
        OpcionTransporteResponse didi = estimadorPrecioAppService.estimarDidi(precioTaxi, tiempoMin, factorClima, distanciaMetros);

        // Guardar en el historial del usuario (si hay uno logueado).
        historialViajeService.guardar(origenFinal, destinoFinal, distanciaMetros, tiempoMin,
                taxi.precio(), uber.precio(), didi.precio(), usuarioId, request);

        List<OpcionTransporteResponse> opciones = List.of(taxi, uber, didi);

        // 💸 ordenar por precio más bajo
        return opciones.stream()
                .sorted(Comparator.comparing(OpcionTransporteResponse::precio))
                .toList();
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
                tiempoMin,
                distanciaMetros,
                "tel:" + telefonoTaxi
        );
    }
}
