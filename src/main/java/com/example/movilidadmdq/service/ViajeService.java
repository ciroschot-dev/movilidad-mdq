package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.enums.TipoTransporte;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ViajeService
{
    // === NUEVO: Inyección de GoogleMapsService ===
    @Autowired
    private GoogleMapsService googleMapsService;

    @Autowired
    private WeatherService weatherService;
    // =============================================

    public List<OpcionTransporteResponse> calcularViaje(String origen, String destino)
    {
        // 🔵 VALORES POR DEFECTO (Simulación / Fallback)
        double distanciaKm = 5.0;
        int tiempoMin = 15;

        // Asegurar que la búsqueda sea en Mar del Plata si no se especificó
        String origenFinal = origen.toLowerCase().contains("mar del plata") ? origen : origen + ", Mar del Plata, Argentina";
        String destinoFinal = destino.toLowerCase().contains("mar del plata") ? destino : destino + ", Mar del Plata, Argentina";

        System.out.println("Solicitando viaje: [" + origenFinal + "] -> [" + destinoFinal + "]");

        // === NUEVO: Intento de obtener datos reales de Google Maps ===
        try {
            DistanceMatrix matrix = googleMapsService.obtenerDatosViaje(origenFinal, destinoFinal);
            
            if (matrix.rows.length > 0 && matrix.rows[0].elements.length > 0) {
                DistanceMatrixElement element = matrix.rows[0].elements[0];
                
                if (element.status.toString().equals("OK") && element.distance != null && element.duration != null) {
                    // Convertir metros a Kilómetros y segundos a Minutos
                    distanciaKm = element.distance.inMeters / 1000.0;
                    tiempoMin = (int) Math.ceil(element.duration.inSeconds / 60.0);
                    
                    System.out.println("✅ Datos REALES obtenidos: " + distanciaKm + "km, " + tiempoMin + "min");
                } else {
                    System.out.println("⚠️ Google Maps no encontró la ruta (Status: " + element.status + "). Usando simulación.");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error Google Maps API: " + e.getMessage());
        }
        // ==========================================================

        BigDecimal precioTaxi = calcularTaxi(distanciaKm);

        List<OpcionTransporteResponse> opciones = List.of
                (
                        construirTaxi(precioTaxi, tiempoMin),
                        construirUber(precioTaxi, tiempoMin, origen, destino),
                        construirDidi(precioTaxi, tiempoMin)
                );

        // 💸 ordenar por precio más bajo
        return opciones.stream()
                .sorted(Comparator.comparing(OpcionTransporteResponse::precioMin))
                .toList();
    }

    // =========================
    // 🚕 TAXI (tarifa real)
    // =========================

    private BigDecimal calcularTaxi(double distanciaKm)
    {
        boolean esNocturno = esHorarioNocturno();

        BigDecimal bajadaBandera;
        BigDecimal valorFicha;
        double metrosPorFicha = 160;
        double distanciaMetros = distanciaKm * 1000;

        if (esNocturno)
        {
            bajadaBandera = BigDecimal.valueOf(2700);
            valorFicha = BigDecimal.valueOf(180);
        }
        else //valores de diurno
        {
            bajadaBandera = BigDecimal.valueOf(2250);
            valorFicha = BigDecimal.valueOf(150);
        }


        // calcular fichas

        double fichasExactas = distanciaMetros / metrosPorFicha;

        int fichas = (int) Math.ceil(fichasExactas); //redondea para arriba

        // calcular precio por fichas

        //multiplica la cant de fichas por el valor de cada ficha (según horario)
        BigDecimal precioFichas = valorFicha.multiply(BigDecimal.valueOf(fichas));

        // precio final
        return bajadaBandera.add(precioFichas); //suma la bajada de bandera + las fichas calculadas
    }

    private boolean esHorarioNocturno()
    {
        int hora = LocalTime.now().getHour();
        return (hora >= 22 || hora < 6); //mayor o igual a 22hrs o antes de las 6
    }

    private OpcionTransporteResponse construirTaxi(BigDecimal precioTaxi, int tiempoMin)
    {
        return new OpcionTransporteResponse(
                TipoTransporte.TAXI,
                precioTaxi,
                precioTaxi,
                tiempoMin,
                generarUrlTaxi()
        );
    }

    // =========================
    // 🚗 UBER
    // =========================

    private OpcionTransporteResponse construirUber(BigDecimal precioTaxi, int tiempoMin, String origen, String destino)
    {
        BigDecimal base = precioTaxi.multiply(BigDecimal.valueOf(0.85)); // base más barato que taxi

        double factorHorario = obtenerFactorHorario();
        double factorClima = obtenerFactorClima();
        double factorDemanda = obtenerFactorDemanda();

        BigDecimal precioMin = base.multiply(BigDecimal.valueOf(factorHorario * factorClima));
        BigDecimal precioMax = base.multiply(BigDecimal.valueOf(factorHorario * factorClima * factorDemanda));

        return new OpcionTransporteResponse(
                TipoTransporte.UBER,
                precioMin.setScale(2, BigDecimal.ROUND_HALF_UP),
                precioMax.setScale(2, BigDecimal.ROUND_HALF_UP),
                tiempoMin,
                generarUrlUber(origen, destino)
        );
    }

    // =========================
    // 🚙 DIDI
    // =========================

    private OpcionTransporteResponse construirDidi(BigDecimal precioTaxi, int tiempoMin)
    {
        BigDecimal base = precioTaxi.multiply(BigDecimal.valueOf(0.75));

        double factorHorario = obtenerFactorHorario();
        double factorClima = obtenerFactorClima();
        double factorDemanda = obtenerFactorDemanda();

        BigDecimal precioMin = base.multiply(BigDecimal.valueOf(factorHorario));
        BigDecimal precioMax = base.multiply(BigDecimal.valueOf(factorHorario * factorClima * factorDemanda));

        return new OpcionTransporteResponse(
                TipoTransporte.DIDI,
                precioMin.setScale(2, BigDecimal.ROUND_HALF_UP),
                precioMax.setScale(2, BigDecimal.ROUND_HALF_UP),
                tiempoMin,
                generarUrlDidi()
        );
    }

    // =========================
    // 🔗 URLs
    // =========================

    private String generarUrlTaxi()
    {
        return "tel:+5492233126129"; // num de Ciro para pruebas. Despues cambiar al de TAXI
    }

    private String generarUrlUber(String origen, String destino)
    {
        return "https://m.uber.com/ul/?action=setPickup" +

                "&pickup[formatted_address]=" + encode(origen) +

                "&dropoff[formatted_address]=" + encode(destino);

    }

    private String generarUrlDidi()
    {
        return "https://www.didiglobal.com/";
    }

    private String encode(String value)
    {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // =========================
    // 📊 FACTORES DINÁMICOS
    // =========================

    private double obtenerFactorHorario()
    {
        int hora = LocalTime.now().getHour();

        if (hora >= 7 && hora <= 9) return 1.3; // hora pico mañana
        if (hora >= 17 && hora <= 20) return 1.4; // hora pico tarde
        if (hora >= 22 || hora < 6) return 1.2; // noche

        return 1.0;
    }

    private double obtenerFactorClima()
    {
        return weatherService.obtenerFactorClima();
    }

    private double obtenerFactorDemanda()
    {
        int autosDisponibles = (int) (Math.random() * 10);

        if (autosDisponibles < 3) return 1.5;
        if (autosDisponibles < 6) return 1.2;

        return 1.0;
    }
}


