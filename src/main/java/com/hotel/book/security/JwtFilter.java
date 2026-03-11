package com.hotel.book.security;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hotel.book.security.JwtUtil;
import com.hotel.book.exception.CustomAuthenticationEntryPoint;
import com.hotel.book.security.JwtAuthenticationException;


@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;


    @Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
        throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {

        String token = authHeader.substring(7);

        try {

            jwtUtil.validateToken(token);

            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            if (email == null || email.isBlank()) {
                throw new JwtAuthenticationException("JWT token missing email claim");
            }

            if (role == null || role.isBlank()) {
                throw new JwtAuthenticationException("JWT token missing role claim");
            }

            List<SimpleGrantedAuthority> authorities =
                    Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + role)
                    );

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (JwtAuthenticationException e) {

            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, e);
            return;

        } catch (Exception e) {

            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new JwtAuthenticationException("Invalid JWT token", e)
            );
            return;
        }
    }

    filterChain.doFilter(request, response);
}
}
