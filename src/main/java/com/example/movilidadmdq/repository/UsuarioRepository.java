package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.enums.Role;
import com.example.movilidadmdq.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acceso a la base de datos para los usuarios.
 * <p>
 * Al heredar de {@link JpaRepository}, Spring Data genera solo la
 * implementación (con los métodos básicos como guardar, borrar y buscar por id)
 * a partir de los nombres de los métodos, sin escribir SQL a mano.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long>
{
    /** Busca por nombre de usuario. Lo usan el login y el filtro de seguridad JWT. */
    Optional<Usuario> findByUsername(String username);

    /** Busca por email. Lo usa el registro para no permitir emails repetidos. */
    Optional<Usuario> findByEmail(String email);

    /**
     * Indica si ya existe algún usuario con ese rol.
     * <p>
     * Lo usa el arranque: si no hay ningún ADMIN, la app crea uno por defecto.
     */
    boolean existsByRole(Role role);
}
