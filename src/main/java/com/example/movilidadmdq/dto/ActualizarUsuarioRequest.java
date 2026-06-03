package com.example.movilidadmdq.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ActualizarUsuarioRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        String password
) {}
