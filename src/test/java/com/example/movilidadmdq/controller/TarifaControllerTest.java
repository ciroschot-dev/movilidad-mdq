package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.enums.Role;
import com.example.movilidadmdq.enums.TipoTransporte;
import com.example.movilidadmdq.model.Tarifa;
import com.example.movilidadmdq.model.Usuario;
import com.example.movilidadmdq.service.TarifaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba el GET de la tarifa del taxi (MDQ-37): el admin ve los valores
 * vigentes para llenar el formulario, y un usuario común no puede.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.config.import="
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TarifaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TarifaService tarifaService;

    @MockitoBean
    private com.example.movilidadmdq.repository.UsuarioRepository usuarioRepository;

    // Arma la tarifa de ejemplo que devuelve el service falso
    private Tarifa tarifaDeEjemplo() {
        Tarifa tarifa = new Tarifa();
        tarifa.setTipoTransporte(TipoTransporte.TAXI);
        tarifa.setBajadaBanderaDia(new BigDecimal("2250.00"));
        tarifa.setBajadaBanderaNoche(new BigDecimal("2700.00"));
        tarifa.setValorFichaDia(new BigDecimal("150.00"));
        tarifa.setValorFichaNoche(new BigDecimal("180.00"));
        tarifa.setMetrosPorFicha(160);
        return tarifa;
    }

    // Arma el usuario que hace la petición, con el rol que se le pase
    private Usuario usuarioConRol(String nombre, Role rol) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername(nombre);
        usuario.setRole(rol);
        return usuario;
    }

    @Test
    void testVerTarifaComoAdminDeberiaDar200ConLosValores() throws Exception {
        Usuario admin = usuarioConRol("admin", Role.ADMIN);
        when(tarifaService.obtenerTarifaTaxi()).thenReturn(tarifaDeEjemplo());
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        mockMvc.perform(get("/admin/tarifas/taxi").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bajadaBanderaDia").value(2250.00))
                .andExpect(jsonPath("$.valorFichaDia").value(150.00))
                .andExpect(jsonPath("$.metrosPorFicha").value(160));
    }

    @Test
    void testVerTarifaComoUsuarioComunDeberiaDar403() throws Exception {
        Usuario usuario = usuarioConRol("testuser", Role.USER);
        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/admin/tarifas/taxi").with(user(usuario)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testVerTarifaSinLoguearseDeberiaDar401() throws Exception {
        mockMvc.perform(get("/admin/tarifas/taxi"))
                .andExpect(status().isUnauthorized());
    }
}
