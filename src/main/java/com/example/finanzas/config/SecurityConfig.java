package com.example.finanzas.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final LoginRateLimitFilter loginRateLimitFilter;
    private final AuthCookies authCookies;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final RestAccessDeniedHandler accessDeniedHandler;

    // Orígenes permitidos para CORS. Configurable por env var CORS_ALLOWED_ORIGINS
    // (lista separada por comas). Por defecto: el front de Vercel y el dev local.
    @Value("${cors.allowed-origins:https://you-finance.vercel.app,http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // El JWT viaja en una cookie httpOnly (a salvo de XSS). Como el navegador la
        // envía sola, protegemos con CSRF double-submit: cookie XSRF-TOKEN legible
        // por JS + cabecera X-XSRF-TOKEN. Usamos el handler "raw" (comparación
        // directa del token) porque es el que encaja con un SPA que lee la cookie y
        // reenvía su valor tal cual. /api/auth/** se excluye (aún no hay sesión).
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        // Carga anticipada del token en CADA petición (en vez del comportamiento
        // diferido por defecto de Spring Security 6). Así el CookieCsrfTokenRepository
        // siempre escribe la cookie XSRF-TOKEN y el SPA la tiene disponible para
        // reenviarla; sin esto, la cookie a veces no se materializa y las escrituras
        // fallan con "no token was found to compare" (MissingCsrfTokenException).
        csrfHandler.setCsrfRequestAttributeName(null);

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .csrfTokenRequestHandler(csrfHandler)
                .ignoringRequestMatchers("/api/auth/**", "/h2-console/**")
            )
            // 403 con el motivo real (CSRF vs. permiso) en JSON, no un genérico.
            .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/h2-console/**", "/error").permitAll()
                // Gestión de feedback: listar y cambiar estado es exclusivo de admin.
                // (Enviar feedback —POST /api/feedback— queda para cualquier autenticado.)
                .requestMatchers(HttpMethod.GET, "/api/feedback").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/feedback/**").hasRole("ADMIN")
                // El resto de recursos van filtrados por el usuario del token
                // (@AuthenticationPrincipal), así que basta con estar autenticado.
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .authenticationProvider(authenticationProvider())
            // Rate limiting del login antes de procesar credenciales.
            .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // Materializa el token CSRF y vuelca la cookie XSRF-TOKEN en cada respuesta.
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);
        return http.build();
    }

    // Los filtros anotados @Component los auto-registra Spring Boot en el contenedor
    // de servlets, lo que los ejecutaría también fuera de la cadena de seguridad
    // (dos veces). Desactivamos ese registro automático para que solo corran donde
    // los añadimos explícitamente en la SecurityFilterChain.
    @Bean
    public FilterRegistrationBean<LoginRateLimitFilter> loginRateLimitFilterRegistration(
            LoginRateLimitFilter filter) {
        FilterRegistrationBean<LoginRateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Repositorio CSRF basado en cookie legible por JS (XSRF-TOKEN), con
     * SameSite/Secure coherentes con la cookie de autenticación. Guarda el token
     * "en crudo", que es lo que el SPA lee y reenvía en la cabecera.
     */
    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repo.setCookieCustomizer(cookie -> cookie
                .secure(authCookies.isSecure())
                .sameSite(authCookies.getSameSite()));
        return repo;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(o -> !o.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // X-XSRF-TOKEN es necesaria para el double-submit de CSRF en cross-site.
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }
}