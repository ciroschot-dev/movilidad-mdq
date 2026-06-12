package com.example.movilidadmdq.enums;

/**
 * Nivel de permisos de un usuario dentro de la app.
 * <p>
 * Spring Security lo usa para decidir a qué cosas puede entrar cada uno.
 * El {@link com.example.movilidadmdq.model.Usuario} lo traduce a un permiso
 * con prefijo (ROLE_USER / ROLE_ADMIN) al momento de validar accesos.
 */
public enum Role
{
    /** Usuario común. Es el rol por defecto al registrarse o entrar con Google. */
    USER,

    /** Administrador. Cuenta con permisos ampliados, se crea al iniciar la app. */
    ADMIN
}
