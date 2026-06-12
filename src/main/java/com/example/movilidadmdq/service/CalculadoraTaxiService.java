package com.example.movilidadmdq.service;

import com.example.movilidadmdq.exception.TarifaIncompletaException;
import com.example.movilidadmdq.model.Tarifa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Calcula el precio del taxi según la tarifa real vigente y el horario.
 *
 * <p>Como cobra el taxi en Mar del Plata:
 *
 * <p>{@code precio_total = bajada_de_bandera + (cantidad_de_fichas * valor_de_ficha)}
 *
 * <p>- Bajada de bandera: monto fijo que se cobra al subir al taxi. Es más cara
 * de noche que de día.
 *
 * <p>- Ficha: unidad de cobro por distancia. Cada N metros suma una ficha. Si el
 * viaje sobra aunque sea un metro, se cobra la ficha completa (por eso usamos
 * {@link Math#ceil}).
 *
 * <p>- Valor de ficha: precio de cada ficha. También es más caro de noche.
 *
 * <p>Los valores se leen de la entidad {@link Tarifa} (tabla "tarifas"), así un admin
 * puede actualizarlos desde {@code PUT /admin/tarifas/taxi} sin redeploy. Tener la
 * fila del taxi cargada y completa es responsabilidad de los datos: si falta un campo,
 * es un error de configuración (no algo que el código deba adivinar con valores
 * hardcodeados que esconderían el problema).
 */
@Service
@RequiredArgsConstructor
public class CalculadoraTaxiService
{
    private final TarifaService tarifaService;

    /** Devuelve el precio del taxi para la distancia dada, aplicando la tarifa diurna o nocturna. */
    public BigDecimal calcularPrecio(double distanciaKm)
    {
        Tarifa tarifa = tarifaService.obtenerTarifaTaxi();
        boolean esNocturno = esHorarioNocturno();

        // De noche cambian tanto la bajada de bandera como el valor de la ficha.
        BigDecimal bajadaBandera = esNocturno ? tarifa.getBajadaBanderaNoche() : tarifa.getBajadaBanderaDia();
        BigDecimal valorFicha    = esNocturno ? tarifa.getValorFichaNoche()    : tarifa.getValorFichaDia();
        Integer metrosPorFicha   = tarifa.getMetrosPorFicha();

        // Si falta algún valor, la tarifa no está bien cargada. Avisamos con un
        // mensaje claro en vez de reventar con un NullPointer más adelante.
        if (bajadaBandera == null || valorFicha == null || metrosPorFicha == null)
        {
            throw new TarifaIncompletaException(
                    "La tarifa del taxi no está completamente cargada. Configurala en PUT /admin/tarifas/taxi.");
        }

        // Math.ceil: cualquier fracción de ficha se cobra como ficha entera.
        int cantidadDeFichas = (int) Math.ceil(distanciaKm * 1000.0 / metrosPorFicha);

        return bajadaBandera.add(valorFicha.multiply(BigDecimal.valueOf(cantidadDeFichas)));
    }

    private boolean esHorarioNocturno()
    {
        int hora = LocalTime.now().getHour();
        return hora >= 22 || hora < 6;
    }
}
