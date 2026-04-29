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

@Service
public class GoogleMapsService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    private GeoApiContext context;

    @PostConstruct
    public void init() {
        context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

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
            throw new RuntimeException("Error al consultar Google Maps API: " + e.getMessage());
        }
    }
}
