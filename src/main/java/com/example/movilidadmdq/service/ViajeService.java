package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.enums.TipoTransporte;
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
    public List<OpcionTransporteResponse> calcularViaje(String origen, String destino)
    {
        // 🔴 Simulación (después agregamos API de Google Maps)
        double distanciaKm = 5.0;
        int tiempoMin = 15;

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
        Boolean esNocturno = esHorarioNocturno();

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

    private Boolean esHorarioNocturno()
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
}
