package com.example.movilidadmdq.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;

@Service
public class WeatherService {

    @Value("${openweather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String CITY = "Mar del Plata,AR";

    public double obtenerFactorClima() {
        try {
            String url = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s", CITY, apiKey);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("weather")) {
                List<Map<String, Object>> weather = (List<Map<String, Object>>) response.get("weather");
                String mainWeather = (String) weather.get(0).get("main");
                
                System.out.println("☁️ Clima actual en MDQ: " + mainWeather);

                return switch (mainWeather.toLowerCase()) {
                    case "thunderstorm" -> 1.5; // Tormenta
                    case "rain", "drizzle" -> 1.3; // Lluvia/Llovizna
                    case "snow" -> 1.4; // Nieve (raro en MDQ pero posible)
                    case "clouds" -> 1.1; // Nublado (ligero aumento)
                    default -> 1.0; // Despejado o similar
                };
            }
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo obtener el clima (usando factor 1.0): " + e.getMessage());
        }
        return 1.0;
    }
}
