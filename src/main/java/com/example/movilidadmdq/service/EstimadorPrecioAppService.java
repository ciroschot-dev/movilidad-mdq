package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.enums.TipoTransporte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;

/**
 * Estima el rango de precio de Uber y Didi a partir del precio del taxi y los
 * factores dinámicos (horario, clima y demanda).
 *
 * <p>Estas apps no publican una tarifa oficial, así que se parte del precio del
 * taxi (que sí es real) con un descuento base, y se le aplican multiplicadores que
 * imitan el "precio dinámico": sube en hora pico, con mal clima y con poca oferta
 * de autos.
 *
 * <p>Evolución futura: si crecen las apps (Cabify, etc.) conviene extraer una
 * interfaz {@code EstimadorApp} con una implementación por app (patrón Strategy +
 * Open/Closed). Hoy no se aplica porque solo hay 2 apps con un cálculo casi idéntico.
 */
@Service
@RequiredArgsConstructor
public class EstimadorPrecioAppService
{
    private final WeatherService weatherService;
    private final UberDeepLinkService uberDeepLinkService;
    private final DidiDeepLinkService didiDeepLinkService;

    // Cada app arranca de un porcentaje del precio del taxi (base más barata).
    private static final BigDecimal BASE_UBER = BigDecimal.valueOf(0.85);
    private static final BigDecimal BASE_DIDI = BigDecimal.valueOf(0.75);

    /**
     * Estima las opciones de Uber y Didi. El clima se consulta una sola vez (es una
     * llamada HTTP) y se comparte entre ambas. Devuelve la lista en orden [UBER, DIDI].
     */
    public List<OpcionTransporteResponse> estimarOpciones(BigDecimal precioTaxi, int tiempoMin,
                                                          CalculoViajeRequest request, long distanciaMetros)
    {
        double factorClima = weatherService.obtenerFactorClima();

        OpcionTransporteResponse uber = estimarUber(precioTaxi, tiempoMin, request, factorClima, distanciaMetros);
        OpcionTransporteResponse didi = estimarDidi(precioTaxi, tiempoMin, factorClima, distanciaMetros);

        return List.of(uber, didi);
    }

    private OpcionTransporteResponse estimarUber(BigDecimal precioTaxi, int tiempoMin, CalculoViajeRequest request,
                                                 double factorClima, long distanciaMetros)
    {
        BigDecimal base = precioTaxi.multiply(BASE_UBER);

        double factorHorario = obtenerFactorHorario();
        double factorDemanda = obtenerFactorDemanda();

        BigDecimal precioMin = base.multiply(BigDecimal.valueOf(factorHorario * factorClima));
        BigDecimal precioMax = base.multiply(BigDecimal.valueOf(factorHorario * factorClima * factorDemanda));

        return new OpcionTransporteResponse(
                TipoTransporte.UBER,
                precioMin.setScale(2, RoundingMode.HALF_UP),
                precioMax.setScale(2, RoundingMode.HALF_UP),
                tiempoMin,
                distanciaMetros,
                uberDeepLinkService.generarUrl(request)
        );
    }

    private OpcionTransporteResponse estimarDidi(BigDecimal precioTaxi, int tiempoMin,
                                                 double factorClima, long distanciaMetros)
    {
        BigDecimal base = precioTaxi.multiply(BASE_DIDI);

        double factorHorario = obtenerFactorHorario();
        double factorDemanda = obtenerFactorDemanda();

        BigDecimal precioMin = base.multiply(BigDecimal.valueOf(factorHorario));
        BigDecimal precioMax = base.multiply(BigDecimal.valueOf(factorHorario * factorClima * factorDemanda));

        return new OpcionTransporteResponse(
                TipoTransporte.DIDI,
                precioMin.setScale(2, RoundingMode.HALF_UP),
                precioMax.setScale(2, RoundingMode.HALF_UP),
                tiempoMin,
                distanciaMetros,
                didiDeepLinkService.generarUrl()
        );
    }

    // El precio sube en los horarios de mayor demanda del día.
    private double obtenerFactorHorario()
    {
        int hora = LocalTime.now().getHour();

        if (hora >= 7 && hora <= 9) return 1.3;   // hora pico mañana
        if (hora >= 17 && hora <= 20) return 1.4; // hora pico tarde
        if (hora >= 22 || hora < 6) return 1.2;   // noche

        return 1.0;
    }

    private double obtenerFactorDemanda()
    {
        int autosDisponibles = (int) (Math.random() * 10);

        if (autosDisponibles < 3) return 1.5;
        if (autosDisponibles < 6) return 1.2;

        return 1.0;
    }
}
