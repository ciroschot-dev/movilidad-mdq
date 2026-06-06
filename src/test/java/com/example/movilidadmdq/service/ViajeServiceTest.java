package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.enums.TipoTransporte;
import com.example.movilidadmdq.model.Tarifa;
import com.example.movilidadmdq.repository.UsuarioRepository;
import com.example.movilidadmdq.repository.ViajeRepository;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.Distance;
import com.google.maps.model.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ViajeServiceTest {

    @Mock
    private GoogleMapsService googleMapsService;

    @Mock
    private WeatherService weatherService;

    @Mock
    private TarifaService tarifaService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ViajeRepository viajeRepository;

    @Mock
    private UberDeepLinkService uberDeepLinkService;

    @Mock
    private DidiDeepLinkService didiDeepLinkService;

    @InjectMocks
    private ViajeService viajeService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(viajeService, "telefonoTaxi", "+542234941010");
    }

    @Test
    void testCalcularViajeExitoso() {
        // GIVEN
        CalculoViajeRequest request = new CalculoViajeRequest(
                "Plaza Mitre", "Estadio Minella",
                null, null, null, null, null,
                null, null, null, null, null
        );

        // Crear elementos del Mock de Google Maps de forma que evite campos final
        DistanceMatrixElement element = new DistanceMatrixElement();
        element.status = com.google.maps.model.DistanceMatrixElementStatus.OK;
        element.distance = new Distance();
        element.distance.inMeters = 5000;
        element.duration = new Duration();
        element.duration.inSeconds = 600;

        DistanceMatrixRow row = new DistanceMatrixRow();
        ReflectionTestUtils.setField(row, "elements", new DistanceMatrixElement[]{element});

        DistanceMatrix matrix = new DistanceMatrix(
                new String[]{"Plaza Mitre"}, 
                new String[]{"Estadio Minella"}, 
                new DistanceMatrixRow[]{row}
        );

        when(googleMapsService.obtenerDatosViaje(anyString(), anyString())).thenReturn(matrix);

        // Mock Tarifa
        Tarifa tarifaTaxi = new Tarifa();
        tarifaTaxi.setPrecioBase(new BigDecimal("2250.00"));
        tarifaTaxi.setPrecioPorKm(new BigDecimal("937.50"));
        when(tarifaService.obtenerTarifaTaxi()).thenReturn(tarifaTaxi);

        // Mock Clima
        when(weatherService.obtenerFactorClima()).thenReturn(1.0);

        // WHEN
        List<OpcionTransporteResponse> resultados = viajeService.calcularViaje(request, 1L);

        // THEN
        assertNotNull(resultados);
        assertFalse(resultados.isEmpty());
        assertEquals(3, resultados.size());

        OpcionTransporteResponse taxi = resultados.stream()
                .filter(r -> r.tipo() == TipoTransporte.TAXI)
                .findFirst()
                .orElseThrow();
        
        assertTrue(taxi.precioMin().compareTo(new BigDecimal("6937")) >= 0);
    }
}
