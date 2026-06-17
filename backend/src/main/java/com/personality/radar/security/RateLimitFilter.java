package com.personality.radar.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Simple in-memory rate limiter for auth endpoints.
 * Production should replace this with Redis-based rate limiting.
 */
public class RateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_MS = 60_000;

    private final Map<String, long[]> requestTimestamps = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/auth/") && !path.startsWith("/api/shares/") && !path.startsWith("/api/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        long[] timestamps = requestTimestamps.computeIfAbsent(clientIp, k -> new long[MAX_REQUESTS_PER_MINUTE]);

        synchronized (timestamps) {
            int count = 0;
            for (long ts : timestamps) {
                if (now - ts < WINDOW_MS) count++;
            }
            if (count >= MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":null}");
                return;
            }
            // Shift and add current timestamp
            System.arraycopy(timestamps, 1, timestamps, 0, MAX_REQUESTS_PER_MINUTE - 1);
            timestamps[MAX_REQUESTS_PER_MINUTE - 1] = now;
        }
        filterChain.doFilter(request, response);
    }
}
