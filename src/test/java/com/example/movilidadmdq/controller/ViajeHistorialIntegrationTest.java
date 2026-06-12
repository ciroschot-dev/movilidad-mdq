package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.enums.Role;
import com.example.movilidadmdq.model.Usuario;
import com.example.movilidadmdq.model.Viaje;
import com.example.movilidadmdq.repository.UsuarioRepository;
import com.example.movilidadmdq.repository.ViajeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb_historial;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.config.import="
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ViajeHistorialIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ViajeRepository viajeRepository;

    @Test
    void testObtenerHistorialDeberiaRetornar200YDatos() throws Exception {
        // GIVEN: Crear usuario y viajes en BD
        Usuario usuario = new Usuario();
        usuario.setUsername("testuser");
        usuario.setPassword("password");
        usuario.setEmail("test@test.com");
        usuario.setRole(Role.USER);
        usuario = usuarioRepository.save(usuario);

        Viaje viaje = new Viaje();
        viaje.setOrigen("Origen");
        viaje.setDestino("Destino");
        viaje.setDistanciaEnMetros(1000L);
        viaje.setTiempoEstimadoMin(10);
        viaje.setFechaHora(LocalDateTime.now());
        viaje.setUsuario(usuario);
        viajeRepository.save(viaje);

        // WHEN & THEN: Llamar al endpoint y verificar
        mockMvc.perform(get("/usuarios/" + usuario.getId() + "/historial")
                .with(user(usuario)) // Pasa el objeto Usuario como principal
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].origen").value("Origen"))
                .andExpect(jsonPath("$[0].destino").value("Destino"));
    }
}
