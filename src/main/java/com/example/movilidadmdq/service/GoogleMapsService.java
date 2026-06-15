package com.example.movilidadmdq.service;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Habla directamente con la API de Google Maps.
 * <p>
 * Su única responsabilidad es consultar una ruta y devolver datos de
 * geolocalización: distancia y tiempo estimado entre origen y destino.
 */
@Service
public class GoogleMapsService {

    // La clave secreta de la API cargada desde las variables de entorno (.env).
    @Value("${google.maps.api.key}")
    private String apiKey;

    // El objeto de contexto que mantiene la conexión y configuración con los servidores de Google.
    private GeoApiContext context;

    /**
     * Arma el cliente de Google con nuestra clave apenas Spring crea el servicio.
     * <p>
     * Corre solo, por la anotación {@code @PostConstruct} (justo después de
     * construir el bean), así la clave ya está cargada cuando se inyecta.
     */
    @PostConstruct
    public void init() {
        context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * Pide a Google la distancia y el tiempo en auto entre origen y destino.
     * <p>
     * Usa la API "Distance Matrix" en modo manejo (considera el tráfico) y
     * unidades métricas, y espera la respuesta de forma sincrónica.
     */
    public DistanceMatrix obtenerDatosViaje(String origen, String destino) {
        try {
            DistanceMatrixApiRequest req = DistanceMatrixApi.getDistanceMatrix(context, 
                new String[]{origen}, 
                new String[]{destino});
            
            return req.mode(TravelMode.DRIVING)
                    .units(Unit.METRIC)
                    .language("es")
                    .await();
        } catch (Exception e) {
            // Si hay un error de red, de cuota de API o de clave inválida, lanzamos una excepción descriptiva.
            throw new RuntimeException("Error al consultar Google Maps API: " + e.getMessage());
        }
    }
}
