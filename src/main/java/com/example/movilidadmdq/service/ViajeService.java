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

/* 
   CLASE: ViajeService
   
   Este servicio es el "Director de Orquesta" del núcleo del sistema. 
   Su misión es coordinar múltiples fuentes de datos para ofrecer al usuario 
   una comparativa de transporte precisa y en tiempo real.
   
   ¿A QUIÉN COORDINA?:
   1. GoogleMapsService: Para obtener la distancia y el tiempo real del tráfico.
   2. WeatherService: Para saber si el clima afecta los precios (factor lluvia).
   3. CalculadoraTaxiService: Para aplicar las tarifas legales de Mar del Plata.
   4. EstimadorPrecioAppService: Para calcular los precios dinámicos de Uber y Didi.
   5. HistorialViajeService: Para registrar la consulta en la base de datos.
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

    /* 
       MÉTODO: calcularViaje
       Es el algoritmo principal que se dispara cuando el usuario consulta un trayecto.
       Orquesta todo el flujo: desde que se pide la distancia hasta que se guarda el resultado.
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

    /* 
       MÉTODO INTERNO: normalizarDireccion
       Agrega el contexto de la ciudad y país si el usuario no lo escribió, 
       optimizando los resultados de búsqueda de Google.
    */
    private String normalizarDireccion(String direccion)
    {
        if (direccion == null || direccion.isBlank()) return "";
        if (direccion.toLowerCase().contains("mar del plata")) return direccion;
        return direccion + ", Mar del Plata, Argentina";
    }

    /* 
       MÉTODO INTERNO: esRespuestaValida
       Valida que el objeto devuelto por Google tenga todos los datos necesarios.
    */
    private boolean esRespuestaValida(DistanceMatrix matrix)
    {
        return matrix != null
                && matrix.rows.length > 0
                && matrix.rows[0].elements.length > 0
                && matrix.rows[0].elements[0].status.toString().equals("OK")
                && matrix.rows[0].elements[0].distance != null
                && matrix.rows[0].elements[0].duration != null;
    }

    /* 
       MÉTODO INTERNO: construirTaxi
       Prepara la respuesta del taxi, incluyendo un Deep Link de tipo 'tel:' 
       para que el usuario pueda llamar a la central directamente.
    */
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
