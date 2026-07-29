package com.example.finanzas.config;

import com.example.finanzas.service.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthCookies authCookies;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String jwt = resolveToken(request);
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        final String username;
        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            logger.debug("[JWT] No se pudo parsear el token: " + e.getClass().getSimpleName());
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // El token puede referirse a un usuario que ya no existe: por ejemplo una
            // cookie emitida por otro entorno que compartía el mismo JWT_TOKEN, o una
            // cuenta borrada. loadUserByUsername lanza UsernameNotFoundException, y si
            // se escapa del filtro Spring responde 500 a TODAS las peticiones (incluido
            // el login, dejando al usuario sin poder entrar). Aquí lo tratamos como
            // "petición no autenticada" y que decida la cadena de seguridad: 401.
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (UsernameNotFoundException e) {
                logger.debug("[JWT] Token de un usuario inexistente: " + username);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Obtiene el JWT de la cookie httpOnly (mecanismo principal). Como respaldo
     * acepta también la cabecera Authorization: Bearer (útil para pruebas y
     * clientes que no usen navegador).
     */
    private String resolveToken(HttpServletRequest request) {
        String cookieToken = authCookies.read(request);
        if (cookieToken != null && !cookieToken.isBlank()) {
            return cookieToken;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
