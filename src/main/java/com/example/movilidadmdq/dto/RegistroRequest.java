package com.example.movilidadmdq.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistroRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank @Email String email
) {}
