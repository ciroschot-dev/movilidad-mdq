package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.enums.Role;
import com.example.movilidadmdq.model.Usuario;
import com.example.movilidadmdq.model.Viaje;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
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
}
