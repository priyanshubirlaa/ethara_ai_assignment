package com.hotel.book.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.book.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiting filter to protect key APIs from abuse.
 * Does NOT change existing controller code or signatures.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    // endpoints to protect
    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/api/auth/login",
            "/api/bookings",          // booking creation/search
            "/api/reviews"            // future review submission
    );

    // key: clientIp + ":" + path, value: deque of request timestamps
    private final Map<String, Deque<Instant>> requestBuckets = new ConcurrentHashMap<>();

    // allow 10 requests per minute per IP per endpoint
    private static final int LIMIT = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!isProtectedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        String key = clientIp + ":" + path;
        Instant now = Instant.now();

        Deque<Instant> bucket = requestBuckets.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (bucket) {
            // remove timestamps older than window
            while (!bucket.isEmpty() && Duration.between(bucket.peekFirst(), now).compareTo(WINDOW) > 0) {
                bucket.pollFirst();
            }

            if (bucket.size() >= LIMIT) {
                log.warn("Rate limit exceeded for key={} ({} requests in {} seconds)", key, bucket.size(), WINDOW.getSeconds());
                respondTooManyRequests(request, response, path);
                return;
            }

            bucket.addLast(now);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isProtectedPath(String path) {
        if (path == null) {
            return false;
        }
        // match exact paths and common prefixes
        return PROTECTED_PATHS.stream().anyMatch(path::startsWith);
    }

    private void respondTooManyRequests(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String path) throws IOException {
        int status = HttpStatus.TOO_MANY_REQUESTS.value();
        response.setStatus(status);
        response.setContentType("application/json");

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .error("Too Many Requests")
                .message("Rate limit exceeded. Please try again later.")
                .path(path)
                .build();

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}

