package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.enums.Role;
import com.example.movilidadmdq.model.DireccionFavorita;
import com.example.movilidadmdq.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DireccionFavoritaRepository direccionFavoritaRepository;

    @Autowired
    private TestEntityManager entityManager;

    // Antes daba error 500: la base no dejaba borrar a un usuario con direcciones favoritas
    @Test
    void borrarUsuarioBorraTambienSusDireccionesFavoritas() {
        // GIVEN un usuario con una dirección favorita
        Usuario usuario = new Usuario();
        usuario.setUsername("con_favorita");
        usuario.setPassword("pass");
        usuario.setEmail("favorita@test.com");
        usuario.setRole(Role.USER);
        usuario = usuarioRepository.save(usuario);

        DireccionFavorita favorita = new DireccionFavorita();
        favorita.setDireccion("Güemes 2500");
        favorita.setUsuario(usuario);
        direccionFavoritaRepository.save(favorita);

        // Se guarda y se vuelve a leer de la base, como pasa cuando alguien borra su cuenta
        entityManager.flush();
        entityManager.clear();
        Long id = usuario.getId();

        // WHEN se borra el usuario
        usuarioRepository.delete(usuarioRepository.findById(id).orElseThrow());
        entityManager.flush();

        // THEN no queda ni el usuario ni su dirección
        assertTrue(usuarioRepository.findById(id).isEmpty());
        assertEquals(0, direccionFavoritaRepository.count());
    }
}
