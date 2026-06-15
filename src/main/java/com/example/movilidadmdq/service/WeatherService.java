package com.example.movilidadmdq.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.List;

/**
 * Consulta el clima actual de Mar del Plata en la API de OpenWeather.
 * <p>
 * Con eso devuelve un "factor multiplicador" de precio: si el clima es adverso
 * (lluvia, tormenta) el viaje sale más caro, para compensar que cuesta más
 * conseguir transporte.
 */
@Service
public class WeatherService
{

    // Clave de API inyectada desde el archivo .env (Seguridad).
    @Value("${openweather.api.key}")
    private String apiKey;

    // RestTemplate es la herramienta de Spring para realizar llamadas HTTP a otras APIs.
    private final RestTemplate restTemplate = new RestTemplate();
    private final String CITY = "Mar del Plata,AR";

    /**
     * Consulta el clima y devuelve el multiplicador de precio.
     * <p>
     * Con lluvia o tormenta da un factor mayor a 1.0; con buen clima, 1.0. Es
     * tolerante a fallos: si la API no responde devuelve 1.0 en vez de romper
     * el cálculo del viaje.
     */
    public double obtenerFactorClima()
    {
        try
        {
            // 1. Llamada a la API externa
            String url = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s", CITY, apiKey);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            // 2. Procesamiento de la respuesta JSON para extraer el estado del clima ('main').
            if (response != null && response.containsKey("weather"))
            {
                List<Map<String, Object>> weather = (List<Map<String, Object>>) response.get("weather");
                String mainWeather = (String) weather.get(0).get("main");

                System.out.println("☁️ Clima actual en MDQ: " + mainWeather);

                // 3. SWITCH DE LÓGICA DE PRECIOS:
                // Define cuánto más caro será el viaje según el riesgo meteorológico.
                return switch (mainWeather.toLowerCase())
                {
                    case "thunderstorm" -> 1.5; // Tormenta (mayor riesgo, mayor precio)
                    case "rain", "drizzle" -> 1.3; // Lluvia/Llovizna
                    case "snow" -> 3.4; // Nieve
                    case "clouds" -> 1.1; // Nublado
                    default -> 1.0; // Despejado (sin aumento)
                };
            }
        }
        catch (Exception e)
        {
            // MANEJO DE FALLOS: Si la API de OpenWeather no responde, 
            // no queremos que el usuario no pueda calcular su viaje. 
            // Simplemente devolvemos el factor 1.0 (sin aumento).
            System.err.println("⚠️ No se pudo obtener el clima (usando factor 1.0): " + e.getMessage());
        }
        return 1.0;
    }
}
