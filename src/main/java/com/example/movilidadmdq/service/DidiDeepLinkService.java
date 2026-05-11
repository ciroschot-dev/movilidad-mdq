package com.example.movilidadmdq.service;

import org.springframework.stereotype.Service;

@Service
public class DidiDeepLinkService {

    public String generarDeepLink(
            String origenNombre, double origenLat, double origenLng,
            String destinoNombre, double destinoLat, double destinoLng
    ) {
        return String.format(
            "https://m.didi.com/ul/?action=setPickup" +
            "&pickup[latitude]=%s&pickup[longitude]=%s&pickup[formatted_address]=%s" +
            "&dropoff[latitude]=%s&dropoff[longitude]=%s&dropoff[formatted_address]=%s",
            origenLat, origenLng, encode(origenNombre),
            destinoLat, destinoLng, encode(destinoNombre)
        );
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
