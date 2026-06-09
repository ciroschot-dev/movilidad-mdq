package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Genera el deep link de Uber para abrir la app con origen y destino precargados.
 * Maneja dos casos:
 * - Si el request trae coordenadas y Place IDs (Google Places), usa el formato
 * rico m.uber.com/go/drop con un JSON templated.
 * - Si no, cae a un deep link simple basado en direcciones de texto.
 */
@Service
public class UberDeepLinkService
{

    public String generarUrl(CalculoViajeRequest request)
    {
        if (request.origenLat() == null || request.origenLng() == null
                || request.destinoLat() == null || request.destinoLng() == null)
        {
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

    private String encode(String value)
    {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
