package com.example.movilidadmdq.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.List;

/* 
   CLASE: WeatherService
   
   Este servicio se encarga de consultar la API de OpenWeather para obtener 
   el estado meteorológico actual en Mar del Plata.
   
   SU FUNCIÓN:
   Determinar un "factor multiplicador" de precio. Si las condiciones son adversas 
   (lluvia, tormenta), el sistema aplica un aumento al precio base del viaje 
   para compensar la dificultad de transporte.
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

    /* 
       MÉTODO: obtenerFactorClima
       Consulta el clima y devuelve un multiplicador para el precio.
       
       LÓGICA:
       - Si llueve o hay tormenta, el precio aumenta (factor > 1.0).
       - Si hay buen clima, el precio se mantiene normal (factor 1.0).
       - Si la API de clima falla, el servicio es "tolerante a fallos": 
         devuelve 1.0 en lugar de romper toda la aplicación.
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
