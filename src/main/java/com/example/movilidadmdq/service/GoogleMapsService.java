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

/* 
   CLASE: GoogleMapsService
   
   Este servicio es la interfaz directa con la API de Google Maps. 
   Su única responsabilidad es realizar consultas técnicas de geolocalización 
   y devolver datos precisos de rutas (distancia y tiempo estimado).
*/
@Service
public class GoogleMapsService {

    // La clave secreta de la API cargada desde las variables de entorno (.env).
    @Value("${google.maps.api.key}")
    private String apiKey;

    // El objeto de contexto que mantiene la conexión y configuración con los servidores de Google.
    private GeoApiContext context;

    /* 
       MÉTODO: init
       Se ejecuta automáticamente después de que Spring crea el servicio (@PostConstruct).
       Configura el cliente de Google inyectando nuestra clave de acceso.
    */
    @PostConstruct
    public void init() {
        context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    /* 
       MÉTODO: obtenerDatosViaje
       Realiza una petición a la API 'Distance Matrix' de Google.
       
       LÓGICA:
       1. DistanceMatrixApi.getDistanceMatrix: Prepara la consulta de origen a destino.
       2. .mode(TravelMode.DRIVING): Especifica que el viaje es en automóvil (considera el tráfico).
       3. .units(Unit.METRIC): Asegura que la distancia venga en metros/kilómetros.
       4. .await(): Realiza la llamada de forma sincrónica, esperando la respuesta oficial de Google.
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
