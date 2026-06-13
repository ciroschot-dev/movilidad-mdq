package com.example.movilidadmdq.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/* 
   CLASE: JwtService
   
   Esta clase es la "Fábrica de Seguridad" de la aplicación. Se encarga de todo el 
   ciclo de vida de los tokens JSON Web Token (JWT).
   
   ¿POR QUÉ JWT?: 
   Nos permite una arquitectura 'Stateless' (sin estado). El servidor no guarda 
   quién está logueado en memoria; toda la prueba de identidad viaja en el token 
   firmado digitalmente que el cliente envía en cada petición.
*/
@Service
public class JwtService
{
    // Clave secreta definida en application.properties para firmar los tokens.
    @Value("${jwt.secret}")
    private String secretKey;

    // Tiempo de vida del token (ej: 24 horas) definido en milisegundos.
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /* 
       MÉTODO: extractUsername
       Recupera el nombre de usuario (el 'subject') que está guardado dentro del token string.
    */
    public String extractUsername(String token)
    {
        return extractClaim(token, Claims::getSubject);
    }

    /* 
       MÉTODO GENÉRICO: extractClaim
       Permite extraer cualquier dato específico (claim) del token usando una función mapeadora.
    */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
    {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /* 
       MÉTODO: generateToken
       Crea un token básico con los datos mínimos del usuario.
    */
    public String generateToken(UserDetails userDetails)
    {
        return generateToken(new HashMap<>(), userDetails);
    }

    /* 
       MÉTODO: generateToken (sobrecargado)
       Permite generar un token incluyendo datos extra personalizados (extraClaims).
    */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails)
    {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /* 
       MÉTODO INTERNO: buildToken
       Construye el string final del JWT con su cabecera, carga útil y firma.
       Utiliza el algoritmo HS256 para la firma digital.
    */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration)
    {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis())) // Fecha de creación
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Fecha de vencimiento
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Firma digital
                .compact();
    }

    /* 
       MÉTODO: isTokenValid
       Verifica que el token pertenezca al usuario que lo envía y que no haya caducado.
    */
    public boolean isTokenValid(String token, UserDetails userDetails)
    {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /* 
       MÉTODO INTERNO: isTokenExpired
       Compara la fecha de expiración del token con la hora actual del servidor.
    */
    private boolean isTokenExpired(String token)
    {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token)
    {
        return extractClaim(token, Claims::getExpiration);
    }

    /* 
       MÉTODO INTERNO: extractAllClaims
       Abre el token usando la clave secreta y lee toda la información que contiene.
       Si el token fue manipulado, este método lanzará una excepción automáticamente.
    */
    private Claims extractAllClaims(String token)
    {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /* 
       MÉTODO INTERNO: getSignInKey
       Prepara la clave secreta en el formato necesario para el algoritmo de firma.
    */
    private Key getSignInKey()
    {
        byte[] keyBytes = decodeBase64SecretOrUseRawText();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /* 
       MÉTODO AUXILIAR: decodeBase64SecretOrUseRawText
       Decodifica la clave secreta. Soporta tanto claves en Base64 como texto plano, 
       haciendo la configuración más flexible.
    */
    private byte[] decodeBase64SecretOrUseRawText()
    {
        try
        {
            Base64.getDecoder().decode(secretKey);
            return Decoders.BASE64.decode(secretKey);
        }
        catch (IllegalArgumentException exception)
        {
            return secretKey.getBytes(StandardCharsets.UTF_8);
        }
    }
}
