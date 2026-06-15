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

/**
 * Crea y valida los tokens JWT con los que la app autentica a sus usuarios.
 * <p>
 * Permite trabajar sin estado (stateless): el servidor no recuerda quién está
 * logueado, toda la prueba de identidad viaja en el token firmado que el cliente
 * manda en cada pedido.
 */
@Service
public class JwtService
{
    // Clave secreta para firmar los tokens, definida en application.properties.
    @Value("${jwt.secret}")
    private String secretKey;

    // Tiempo de vida del token, en milisegundos.
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /** Devuelve el nombre de usuario guardado dentro del token. */
    public String extractUsername(String token)
    {
        return extractClaim(token, Claims::getSubject);
    }

    /** Extrae cualquier dato (claim) del token usando una función que lo mapea. */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
    {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /** Crea un token con los datos mínimos del usuario. */
    public String generateToken(UserDetails userDetails)
    {
        return generateToken(new HashMap<>(), userDetails);
    }

    /** Igual que el anterior, pero permite sumarle datos extra al token. */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails)
    {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    // Arma el string final del token (cabecera, datos y firma) usando HS256.
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

    /** Verifica que el token sea del usuario que lo manda y que no haya vencido. */
    public boolean isTokenValid(String token, UserDetails userDetails)
    {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Compara el vencimiento del token con la hora actual del servidor.
    private boolean isTokenExpired(String token)
    {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token)
    {
        return extractClaim(token, Claims::getExpiration);
    }

    // Abre el token con la clave secreta y lee todo su contenido. Si fue
    // manipulado, parseClaimsJws tira una excepción.
    private Claims extractAllClaims(String token)
    {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Prepara la clave secreta en el formato que pide el algoritmo de firma.
    private Key getSignInKey()
    {
        byte[] keyBytes = decodeBase64SecretOrUseRawText();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Decodifica la clave secreta. Acepta tanto Base64 como texto plano, así la
    // configuración es más flexible.
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
