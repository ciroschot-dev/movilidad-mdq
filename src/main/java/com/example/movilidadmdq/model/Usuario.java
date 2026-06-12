package com.example.movilidadmdq.model;

import com.example.movilidadmdq.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Usuario registrado en la app.
 * <p>
 * Guarda los datos de la cuenta (nombre, contraseña, email, rol) y la
 * lista de viajes que hizo. Implementa {@link UserDetails} para que
 * Spring Security pueda usarla directamente al iniciar sesión y al
 * validar permisos en cada pedido.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "usuarios")
public class Usuario implements UserDetails
{
    // === Identificador ===

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === Datos de la cuenta ===

    /**
     * Nombre con el que el usuario inicia sesión. Único en la base.
     */
    @Column(length = 150, nullable = false, unique = true)
    private String username;

    /**
     * Contraseña guardada encriptada (nunca en texto plano).
     */
    @Column(length = 150, nullable = false)
    private String password;

    /**
     * Email del usuario. Único, validado como mail real.
     */
    @NotBlank
    @Email
    @Column(length = 150, nullable = false, unique = true)
    private String email;

    /**
     * Rol del usuario (define qué cosas puede hacer dentro de la app).
     */
    @Enumerated(EnumType.STRING)
    private Role role;

    // === Relación con sus viajes ===

    /**
     * Lista de viajes que hizo este usuario.
     * <p>
     * Si el usuario se elimina, sus viajes también se eliminan
     * (cascade + orphanRemoval).
     */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Viaje> viajes;

    // === Integración con Spring Security ===

    /**
     * Devuelve los permisos del usuario a partir de su rol.
     * <p>
     * Si el rol está vacío, se asume USER por defecto. Spring Security
     * usa esta lista para decidir si puede entrar a cada endpoint.
     */
    @NullMarked
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        Role rolActual = role == null ? Role.USER : role;
        return List.of(new SimpleGrantedAuthority("ROLE_" + rolActual.name()));
    }

    // Los métodos de abajo son los "candados" estándar de Spring Security.
    // Devuelven todos true (cuenta activa, no bloqueada, no vencida) porque no
    // usamos expiración de cuentas ni bloqueos manuales en esta app.

    @Override
    public boolean isAccountNonExpired()
    {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled()
    {
        return UserDetails.super.isEnabled();
    }
}
