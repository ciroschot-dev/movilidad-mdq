package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.enums.Role;
import com.example.movilidadmdq.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>
{
    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    boolean existsByRole(Role role);
}
