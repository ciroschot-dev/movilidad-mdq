package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.enums.TipoTransporte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;

/**
 * Estima el precio de Uber y Didi a partir del precio del taxi y los factores
 * dinámicos (horario y clima).
 *
 * <p>Estas apps no publican una tarifa oficial, así que se parte del precio del
 * taxi (que sí es real) con un descuento base, y se le aplican multiplicadores que
 * imitan el "precio dinámico": sube en hora pico y con mal clima.
 *
 * <p>Evolución futura: si crecen las apps (Cabify, etc.) conviene extraer una
 * interfaz {@code EstimadorApp} con una implementación por app (patrón Strategy +
 * Open/Closed). Hoy no se aplica porque solo hay 2 apps con un cálculo casi idéntico.
 */
@Service
@RequiredArgsConstructor
public class EstimadorPrecioAppService
{
    private final UberDeepLinkService uberDeepLinkService;
    private final DidiDeepLinkService didiDeepLinkService;

    // Cada app arranca de un porcentaje del precio del taxi (base más barata).
    private static final BigDecimal BASE_UBER = BigDecimal.valueOf(0.85);
    private static final BigDecimal BASE_DIDI = BigDecimal.valueOf(0.75);

    /**
     * Estima el precio de Uber. El factor de clima lo calcula el orquestador y lo pasa ya resuelto.
     */
    public OpcionTransporteResponse estimarUber(BigDecimal precioTaxi, int tiempoMin, double factorClima,
                                                long distanciaMetros, CalculoViajeRequest request)
    {
        return estimarApp(TipoTransporte.UBER, BASE_UBER, uberDeepLinkService.generarUrl(request),
                precioTaxi, tiempoMin, factorClima, distanciaMetros);
    }

    /**
     * Estima el precio de Didi. El factor de clima lo calcula el orquestador y lo pasa ya resuelto.
     */
    public OpcionTransporteResponse estimarDidi(BigDecimal precioTaxi, int tiempoMin, double factorClima,
                                                long distanciaMetros)
    {
        return estimarApp(TipoTransporte.DIDI, BASE_DIDI, didiDeepLinkService.generarUrl(),
                precioTaxi, tiempoMin, factorClima, distanciaMetros);
    }

    // Uber y Didi se estiman igual; solo cambian el factor base, el tipo y el deep link.
    private OpcionTransporteResponse estimarApp(TipoTransporte tipo, BigDecimal factorBase, String url,
                                                BigDecimal precioTaxi, int tiempoMin, double factorClima, long distanciaMetros)
    {
        BigDecimal base = precioTaxi.multiply(factorBase);
        BigDecimal precio = aplicarFactores(base, factorClima);

        return new OpcionTransporteResponse(tipo, precio, tiempoMin, distanciaMetros, url);
    }

    // Aplica los factores dinámicos sobre el precio base y redondea a 2 decimales.
    private BigDecimal aplicarFactores(BigDecimal base, double factorClima)
    {
        double factorHorario = obtenerFactorHorario();
        return base.multiply(BigDecimal.valueOf(factorHorario * factorClima))
                .setScale(2, RoundingMode.HALF_UP);
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
}
