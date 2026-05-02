package com.example.movilidadmdq;

import com.example.movilidadmdq.dto.OpcionTransporteResponse;
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
        
        System.out.println("--- INICIANDO TEST DE DEPURACIÓN ---");
        List<OpcionTransporteResponse> resultados = viajeService.calcularViaje(origen, destino);
        
        assertNotNull(resultados);
        for (OpcionTransporteResponse opcion : resultados) {
            System.out.println("Tipo: " + opcion.getTipo() + 
                               " | Precio: " + opcion.getPrecioMin() + " - " + opcion.getPrecioMax() + 
                               " | Tiempo: " + opcion.getTiempoMinutos() + " min");
        }
        System.out.println("--- FIN TEST DE DEPURACIÓN ---");
    }
}
