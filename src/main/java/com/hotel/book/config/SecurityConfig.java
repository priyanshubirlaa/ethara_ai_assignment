package com.hotel.book.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hotel.book.exception.CustomAccessDeniedHandler;
import com.hotel.book.exception.CustomAuthenticationEntryPoint;
import com.hotel.book.security.JwtFilter;


@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

       http.csrf(csrf -> csrf.disable())
       .exceptionHandling(exception -> exception
        .authenticationEntryPoint(authenticationEntryPoint)
        .accessDeniedHandler(accessDeniedHandler)
    )
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/login").permitAll()

        // ADMIN only
        .requestMatchers("/api/hotels/**").hasRole("ADMIN")
        .requestMatchers("/api/hotels/*/rooms/**").hasRole("ADMIN")

        // STAFF & ADMIN
        .requestMatchers("/api/customers/**").hasAnyRole("ADMIN", "STAFF")
        .requestMatchers("/api/bookings/**").hasAnyRole("ADMIN", "STAFF")

        .requestMatchers("/api/health","/swagger-ui/**", "/v3/**", "/actuator/**").permitAll()
        .requestMatchers("/api/auth/register").hasRole("ADMIN")
        

        .anyRequest().authenticated()
    )
    .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    );

        http.addFilterBefore(jwtFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
     @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
