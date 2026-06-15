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
 * Coordina el cálculo de un viaje: es el "director de orquesta" del núcleo.
 * <p>
 * Junta los datos de varios servicios especialistas para devolver la
 * comparativa de transporte en tiempo real:
 * <ul>
 *   <li>{@code GoogleMapsService}: distancia y tiempo según el tráfico.</li>
 *   <li>{@code WeatherService}: si el clima encarece el viaje (factor lluvia).</li>
 *   <li>{@code CalculadoraTaxiService}: la tarifa legal del taxi de MDQ.</li>
 *   <li>{@code EstimadorPrecioAppService}: los precios estimados de Uber y Didi.</li>
 *   <li>{@code HistorialViajeService}: guarda la consulta en la base.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ViajeService
{
    // === Inyecciones de Dependencia (Servicios Especialistas) ===
    private final GoogleMapsService googleMapsService;
    private final WeatherService weatherService;
    private final CalculadoraTaxiService calculadoraTaxiService;
    private final EstimadorPrecioAppService estimadorPrecioAppService;
    private final HistorialViajeService historialViajeService;

    // Teléfono de la central de taxis, cargado desde la configuración.
    @Value("${taxi.telefono:+542234941010}")
    private String telefonoTaxi;

    /**
     * Calcula y compara las opciones de transporte para un trayecto.
     * <p>
     * Es el flujo principal que se dispara cuando el usuario consulta un viaje:
     * pide la distancia, aplica el clima, calcula los precios, guarda la
     * consulta y devuelve las opciones ordenadas de más barata a más cara.
     */
    public List<OpcionTransporteResponse> calcularViaje(CalculoViajeRequest request, Long usuarioId)
    {
        String origen = request.origen();
        String destino = request.destino();

        // VALORES POR DEFECTO (Simulación / Fallback):
        // Se utilizan si la API de Google Maps falla para no romper la experiencia del usuario.
        double distanciaKm = 5.0;
        int tiempoMin = 15;

        // 1. NORMALIZACIÓN: Asegura que la búsqueda se realice en el contexto de Mar del Plata.
        String origenFinal = normalizarDireccion(origen);
        String destinoFinal = normalizarDireccion(destino);

        System.out.println("Solicitando viaje: [" + origenFinal + "] -> [" + destinoFinal + "]");

        // 2. CONSULTA A GOOGLE MAPS:
        // Intenta obtener datos reales de tráfico y distancia.
        try
        {
            DistanceMatrix matrix = googleMapsService.obtenerDatosViaje(origenFinal, destinoFinal);
            if (esRespuestaValida(matrix))
            {
                DistanceMatrixElement element = matrix.rows[0].elements[0];

                // Conversión de unidades técnicas a unidades de negocio (Metros -> Km, Segundos -> Min).
                distanciaKm = element.distance.inMeters / 1000.0;
                tiempoMin = (int) Math.ceil(element.duration.inSeconds / 60.0);

                System.out.println("Datos REALES obtenidos: " + distanciaKm + "km, " + tiempoMin + "min");
            }
        }
        catch (Exception e)
        {
            // Error controlado: si falla la API, la app sigue funcionando con los valores de fallback.
            System.err.println("Error Google Maps API: " + e.getMessage());
        }

        long distanciaMetros = (long) (distanciaKm * 1000);

        // 3. SEÑALES EXTERNAS Y CÁLCULO DE PRECIOS:
        // Obtiene el factor clima (lluvia) y calcula el precio del taxi basado en km.
        BigDecimal precioTaxi = calculadoraTaxiService.calcularPrecio(distanciaKm);
        double factorClima = weatherService.obtenerFactorClima();

        // 4. CONSTRUCCIÓN DE RESPUESTAS:
        // Crea los objetos con precios, tiempos y URLs de Deep Linking para cada app.
        OpcionTransporteResponse taxi = construirTaxi(precioTaxi, tiempoMin, distanciaMetros);
        OpcionTransporteResponse uber = estimadorPrecioAppService.estimarUber(precioTaxi, tiempoMin, factorClima, distanciaMetros, request);
        OpcionTransporteResponse didi = estimadorPrecioAppService.estimarDidi(precioTaxi, tiempoMin, factorClima, distanciaMetros);

        // 5. PERSISTENCIA:
        // Registra el viaje en el historial del usuario antes de enviar la respuesta.
        historialViajeService.guardar(origenFinal, destinoFinal, distanciaMetros, tiempoMin,
                taxi.precio(), uber.precio(), didi.precio(), usuarioId, request);

        List<OpcionTransporteResponse> opciones = List.of(taxi, uber, didi);

        // 6. ORDENAMIENTO:
        // Devuelve las opciones ordenadas por el precio más bajo para ayudar al usuario a ahorrar.
        return opciones.stream()
                .sorted(Comparator.comparing(OpcionTransporteResponse::precio))
                .toList();
    }

    // Agrega ", Mar del Plata, Argentina" si el usuario no lo escribió, para que
    // Google no busque la calle en otra ciudad.
    private String normalizarDireccion(String direccion)
    {
        if (direccion == null || direccion.isBlank()) return "";
        if (direccion.toLowerCase().contains("mar del plata")) return direccion;
        return direccion + ", Mar del Plata, Argentina";
    }

    // Chequea que la respuesta de Google traiga distancia y tiempo válidos
    // antes de leerlos, para no caer en un NullPointer.
    private boolean esRespuestaValida(DistanceMatrix matrix)
    {
        return matrix != null
                && matrix.rows.length > 0
                && matrix.rows[0].elements.length > 0
                && matrix.rows[0].elements[0].status.toString().equals("OK")
                && matrix.rows[0].elements[0].distance != null
                && matrix.rows[0].elements[0].duration != null;
    }

    // Arma la opción del taxi. El "deep link" es un tel: para que el usuario
    // pueda llamar a la central tocando el resultado.
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
