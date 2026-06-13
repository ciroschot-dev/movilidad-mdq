package com.example.movilidadmdq.repository;

import com.example.movilidadmdq.enums.Role;
import com.example.movilidadmdq.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/* 
   INTERFAZ: UsuarioRepository
   
   Esta interfaz utiliza Spring Data JPA para gestionar la persistencia de los usuarios.
   Hereda de JpaRepository, lo que nos otorga automáticamente métodos CRUD básicos como 
   save(), delete() y findById() sin escribir una sola línea de SQL.
   
   ¿POR QUÉ INTERFAZ?:
   Spring Boot detecta esta interfaz y genera automáticamente la implementación necesaria 
   en tiempo de ejecución basándose en los nombres de los métodos.
*/
public interface UsuarioRepository extends JpaRepository<Usuario, Long>
{
    /* 
       MÉTODO: findByUsername
       Busca un usuario por su nombre de usuario único.
       Se usa principalmente en el proceso de Login y en el filtro de seguridad JWT.
       Retorna un 'Optional' para evitar errores de puntero nulo si el usuario no existe.
    */
    Optional<Usuario> findByUsername(String username);

    /* 
       MÉTODO: findByEmail
       Busca un usuario por su dirección de correo.
       Se utiliza en el registro de nuevos usuarios para garantizar que no haya emails duplicados.
    */
    Optional<Usuario> findByEmail(String email);

    /* 
       MÉTODO: existsByRole
       Verifica de forma eficiente si existe en la base de datos algún usuario con un rol dado.
       Es fundamental para el arranque del sistema (Bootstrap): si no existe ningún ADMIN, 
       la app crea uno por defecto.
    */
    boolean existsByRole(Role role);
}
