package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>
{
}
