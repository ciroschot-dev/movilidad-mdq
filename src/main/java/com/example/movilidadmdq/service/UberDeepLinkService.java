package com.example.movilidadmdq.service;

import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class UberDeepLinkService {

    public String generarDeepLink(
            String origenNombre, double origenLat, double origenLng,
            String destinoNombre, double destinoLat, double destinoLng
    ) {
        return String.format(
                "uber://?action=setPickup" +
                        "&pickup[latitude]=%s&pickup[longitude]=%s&pickup[formatted_address]=%s" +
                        "&dropoff[latitude]=%s&dropoff[longitude]=%s&dropoff[formatted_address]=%s",
                origenLat, origenLng, encode(origenNombre),
                destinoLat, destinoLng, encode(destinoNombre)
        );
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}