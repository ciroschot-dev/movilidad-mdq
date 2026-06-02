package com.example.movilidadmdq.dto;

import com.example.movilidadmdq.enums.Role;

public record AuthResponse(Long id, String username, String email, String token, Role role) {}
