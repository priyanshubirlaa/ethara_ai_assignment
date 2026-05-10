package com.hotel.book.config;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.book.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Populates MDC for structured logging with request-scoped metadata
 * like correlation ID, user, role and endpoint.
 * Also short-circuits clearly missing JWT cases for secured API endpoints
 * with a consistent ErrorResponse payload.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {

    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_USER = "user";
    private static final String MDC_ROLE = "role";
    private static final String MDC_ENDPOINT = "endpoint";

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // If this is a secured API endpoint and there is NO Authorization header at all,
        // return a clear 401 JSON error immediately.
        if (isSecuredApi(request)
                && (request.getHeader("Authorization") == null
                    || request.getHeader("Authorization").isBlank())) {

            int status = HttpServletResponse.SC_UNAUTHORIZED;
            response.setStatus(status);
            response.setContentType("application/json");

            ErrorResponse body = ErrorResponse.builder()
                    .timestamp(Instant.now())
                    .status(status)
                    .error("Unauthorized")
                    .message("Missing JWT token. Please include a valid Bearer token in the Authorization header.")
                    .path(uri)
                    .build();

            objectMapper.writeValue(response.getOutputStream(), body);
            return;
        }

        String requestId = UUID.randomUUID().toString();
        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_ENDPOINT, request.getMethod() + " " + uri);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = "anonymous";
        String role = "NONE";

        if (authentication != null && authentication.isAuthenticated()) {
            user = authentication.getName();
            role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("NONE");
        }

        MDC.put(MDC_USER, user);
        MDC.put(MDC_ROLE, role);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private boolean isSecuredApi(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        if (uri == null) {
            return false;
        }

        // All /api/** except the explicitly public endpoints
        if (!uri.startsWith("/api/")) {
            return false;
        }

        if (uri.startsWith("/api/auth/login")
                || uri.startsWith("/api/auth/register")
                || uri.startsWith("/api/health")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/")) {
            return false;
        }

        return true;
    }
}

