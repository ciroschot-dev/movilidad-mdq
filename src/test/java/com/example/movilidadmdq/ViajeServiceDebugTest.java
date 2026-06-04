package com.example.movilidadmdq;

import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.service.ViajeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ViajeServiceDebugTest {

    @Autowired
    private ViajeService viajeService;

    @Test
    void testCalcularViajeReal() {
        String origen = "Colon 2090, Mar del Plata";
        String destino = "Colon 3132, Mar del Plata";

        System.out.println("--- INICIANDO TEST DE DEPURACION ---");
        List<OpcionTransporteResponse> resultados = viajeService.calcularViaje(
                new CalculoViajeRequest(
                        origen,
                        destino,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                1L
        );
        assertNotNull(resultados);
        for (OpcionTransporteResponse opcion : resultados) {
            System.out.println(
                    "Tipo: " + opcion.tipo() +
                            " | Precio: " + opcion.precioMin() + " - " + opcion.precioMax() +
                            " | Tiempo: " + opcion.tiempoMinutos() + " min"
            );
        }
        System.out.println("--- FIN TEST DE DEPURACION ---");
    }
}