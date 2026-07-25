package com.example.finanzas.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting sencillo (ventana fija en memoria) para POST /api/auth/login,
 * pensado para frenar ataques de fuerza bruta. Limita el nº de intentos por IP
 * dentro de una ventana temporal; superado el límite responde 429.
 *
 * Limitaciones conocidas: el contador es por instancia (no distribuido). Si se
 * despliega en varias réplicas o detrás de un balanceador conviene mover esto a
 * un almacén compartido (Redis) o a un WAF/gateway. Configurable con:
 *  - auth.login.max-attempts    (por defecto 10)
 *  - auth.login.window-seconds  (por defecto 300)
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/auth/login";

    private final int maxAttempts;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Window> buckets = new ConcurrentHashMap<>();

    public LoginRateLimitFilter(
            @Value("${auth.login.max-attempts:10}") int maxAttempts,
            @Value("${auth.login.window-seconds:300}") long windowSeconds) {
        this.maxAttempts = maxAttempts;
        this.windowMillis = windowSeconds * 1000L;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = clientIp(request);
        long now = System.currentTimeMillis();
        if (buckets.size() > 10_000) {
            buckets.values().removeIf(w -> now - w.start > windowMillis);
        }

        Window window = buckets.compute(ip, (k, existing) -> {
            if (existing == null || now - existing.start > windowMillis) {
                return new Window(now);
            }
            return existing;
        });
        int intentos = window.count.incrementAndGet();

        if (intentos > maxAttempts) {
            long retryAfter = Math.max(1, (windowMillis - (now - window.start)) / 1000);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"detail\":\"Demasiados intentos de inicio de sesión. Inténtalo de nuevo en "
                            + retryAfter + " segundos.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && LOGIN_PATH.equals(request.getRequestURI());
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // La IP real del cliente es la primera de la lista.
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class Window {
        private final long start;
        private final AtomicInteger count = new AtomicInteger(0);

        private Window(long start) {
            this.start = start;
        }
    }
}
