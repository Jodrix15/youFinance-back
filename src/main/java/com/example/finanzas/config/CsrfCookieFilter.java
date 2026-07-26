package com.example.finanzas.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * En Spring Security 6 el token CSRF se genera de forma diferida (solo cuando
 * se accede a él). Este filtro "toca" el token en cada petición para forzar que
 * el CookieCsrfTokenRepository escriba la cookie XSRF-TOKEN, de modo que el SPA
 * pueda leerla y reenviarla en la cabecera X-XSRF-TOKEN.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken(); // fuerza la carga y el volcado de la cookie
        }
        filterChain.doFilter(request, response);
    }
}
