package com.example.movilidadmdq.dto;

import com.example.movilidadmdq.enums.Role;

public record UsuarioResponse(Long id, String username, String email, Role role) {}
