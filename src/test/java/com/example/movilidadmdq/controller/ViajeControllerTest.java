package com.example.movilidadmdq.controller;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.enums.Role;
import com.example.movilidadmdq.model.Usuario;
import com.example.movilidadmdq.service.ViajeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ViajeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ViajeService viajeService;

    @MockitoBean
    private com.example.movilidadmdq.repository.UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCalcularViajeSinAuthDeberiaDar401() throws Exception {
        CalculoViajeRequest request = new CalculoViajeRequest(
                "Origen", "Destino",
                null, null, null, null, null,
                null, null, null, null, null
        );

        mockMvc.perform(post("/viajes/calcular")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCalcularViajeConAuthDeberiaDar200() throws Exception {
        CalculoViajeRequest request = new CalculoViajeRequest(
                "Origen", "Destino",
                null, null, null, null, null,
                null, null, null, null, null
        );

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("testuser");
        usuario.setRole(Role.USER);

        when(usuarioRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(usuario));
        when(viajeService.calcularViaje(any(), eq(1L))).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/viajes/calcular")
                .with(user(usuario)) // Simula el principal como objeto Usuario
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
