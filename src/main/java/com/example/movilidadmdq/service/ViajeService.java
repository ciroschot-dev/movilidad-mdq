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
    // =============================================

    public List<OpcionTransporteResponse> calcularViaje(String origen, String destino)
    {
        // 🔵 VALORES POR DEFECTO (Simulación / Fallback)
        double distanciaKm = 5.0;
        int tiempoMin = 15;

        // === NUEVO: Intento de obtener datos reales de Google Maps ===
        try {
            DistanceMatrix matrix = googleMapsService.obtenerDatosViaje(origen, destino);
            
            if (matrix.rows.length > 0 && matrix.rows[0].elements.length > 0) {
                DistanceMatrixElement element = matrix.rows[0].elements[0];
                
                if (element.distance != null && element.duration != null) {
                    // Convertir metros a Kilómetros y segundos a Minutos
                    distanciaKm = element.distance.inMeters / 1000.0;
                    tiempoMin = (int) Math.ceil(element.duration.inSeconds / 60.0);
                    
                    System.out.println("Datos reales obtenidos: " + distanciaKm + "km, " + tiempoMin + "min");
                }
            }
        } catch (Exception e) {
            System.err.println("Google Maps API no disponible (usando simulación): " + e.getMessage());
            // Si falla la API, se mantienen los valores de 5.0km y 15min por defecto
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
                .sorted(Comparator.comparing(OpcionTransporteResponse::getPrecioMin))
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
        BigDecimal precioMin = precioTaxi.multiply(BigDecimal.valueOf(0.9)); // 10% más barato

        return new OpcionTransporteResponse(
                TipoTransporte.UBER,
                precioMin,
                precioTaxi, //precioMax = taxi
                tiempoMin,
                generarUrlUber(origen, destino)
        );
    }

    // =========================
    // 🚙 DIDI
    // =========================

    private OpcionTransporteResponse construirDidi(BigDecimal precioTaxi, int tiempoMin)
    {
        BigDecimal precioMin = precioTaxi.multiply(BigDecimal.valueOf(0.75)); // bastante más barato

        BigDecimal precioMax = precioTaxi.multiply(BigDecimal.valueOf(0.9));

        return new OpcionTransporteResponse(
                TipoTransporte.DIDI,
                precioMin,
                precioMax,
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

    /*
    UBER NUEVO
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
            precioMin,
            precioMax,
            tiempoMin,
            generarUrlUber(origen, destino)
    );
}


DIDI NUEVO
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
            precioMin,
            precioMax,
            tiempoMin,
            generarUrlDidi()
    );
}

FACTOR HORARIO

private double obtenerFactorHorario()
{
    int hora = LocalTime.now().getHour();

    if (hora >= 7 && hora <= 9) return 1.3; // hora pico mañana
    if (hora >= 17 && hora <= 20) return 1.4; // hora pico tarde
    if (hora >= 22 || hora < 6) return 1.2; // noche

    return 1.0;
}

FACTOR CLIMA

private double obtenerFactorClima()
{
    // después lo conectamos con API real
    double random = Math.random();

    if (random < 0.2) return 1.5; // lluvia fuerte
    if (random < 0.4) return 1.2; // llovizna

    return 1.0;
}

FACTOR DEMANDA

private double obtenerFactorDemanda()
{
    int autosDisponibles = (int) (Math.random() * 10);

    if (autosDisponibles < 3) return 1.5;
    if (autosDisponibles < 6) return 1.2;

    return 1.0;
}









*/

}


