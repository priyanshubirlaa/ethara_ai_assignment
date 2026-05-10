package com.hotel.book.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    
    private static final String SECRET =
            "thisisaverysecuresecretkeythisisaverysecuresecretkey";

    private static final long EXPIRATION = 1000 * 60 * 60 * 24; 

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

  
    public String generateToken(String email, String role) {

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

   
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

   
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

   
    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    

    public void validateToken(String token) {

        try {

            Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);

        } catch (ExpiredJwtException ex) {

            throw new JwtAuthenticationException("JWT token expired", ex);

        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtAuthenticationException("Invalid JWT token", ex);
        }
    }

}