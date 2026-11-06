package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.dto.DestinoPopularResponse;
import com.example.movilidadmdq.enums.Role;
import com.example.movilidadmdq.model.Usuario;
import com.example.movilidadmdq.model.Viaje;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ViajeRepositoryTest {

    @Autowired
    private ViajeRepository viajeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void testFindByUsuarioIdOrderByFechaHoraDesc() {
        // GIVEN
        Usuario usuario = new Usuario();
        usuario.setUsername("tester");
        usuario.setPassword("pass");
        usuario.setEmail("test@test.com");
        usuario.setRole(Role.USER);
        usuario = usuarioRepository.save(usuario);

        Viaje viaje1 = new Viaje();
        viaje1.setOrigen("A");
        viaje1.setDestino("B");
        viaje1.setDistanciaEnMetros(1000L);
        viaje1.setTiempoEstimadoMin(10);
        viaje1.setPrecioTaxi(new BigDecimal("1000"));
        viaje1.setUsuario(usuario);
        viajeRepository.save(viaje1);

        Viaje viaje2 = new Viaje();
        viaje2.setOrigen("C");
        viaje2.setDestino("D");
        viaje2.setDistanciaEnMetros(2000L);
        viaje2.setTiempoEstimadoMin(20);
        viaje2.setPrecioTaxi(new BigDecimal("2000"));
        viaje2.setUsuario(usuario);
        viajeRepository.save(viaje2);

        // WHEN
        List<Viaje> resultados = viajeRepository.findByUsuarioIdOrderByFechaHoraDesc(usuario.getId());

        // THEN
        assertEquals(2, resultados.size());
        assertEquals("C", resultados.get(0).getOrigen()); // El último guardado debería ser el primero por fechaHora desc
    }

    @Test
    void testFindPopularDestinationsCuentaYOrdena() {
        // GIVEN un usuario con tres viajes, dos hacia el mismo destino
        Usuario usuario = new Usuario();
        usuario.setUsername("tester2");
        usuario.setPassword("pass");
        usuario.setEmail("test2@test.com");
        usuario.setRole(Role.USER);
        usuario = usuarioRepository.save(usuario);

        viajeRepository.save(crearViaje(usuario, "Plaza Mitre"));
        viajeRepository.save(crearViaje(usuario, "Plaza Mitre"));
        viajeRepository.save(crearViaje(usuario, "Playa Grande"));

        // WHEN se pide el ranking sin fechas (el service completa los valores por defecto)
        List<DestinoPopularResponse> ranking = viajeRepository.findPopularDestinations(
                LocalDateTime.of(1970, 1, 1, 0, 0),
                LocalDateTime.of(2999, 12, 31, 23, 59),
                PageRequest.of(0, 10));

        // THEN el que más se buscó va primero, con su cantidad
        assertEquals(2, ranking.size());
        assertEquals("Plaza Mitre", ranking.get(0).destino());
        assertEquals(2L, ranking.get(0).cantidad());
    }

    // Arma un viaje mínimo, que es todo lo que la consulta del ranking necesita
    private Viaje crearViaje(Usuario usuario, String destino) {
        Viaje viaje = new Viaje();
        viaje.setOrigen("Origen");
        viaje.setDestino(destino);
        viaje.setDistanciaEnMetros(1000L);
        viaje.setTiempoEstimadoMin(10);
        viaje.setPrecioTaxi(new BigDecimal("1000"));
        viaje.setUsuario(usuario);
        return viaje;
    }
}
