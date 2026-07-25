package com.example.finanzas.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Gestiona la cookie httpOnly que transporta el JWT. Al ser httpOnly, el
 * JavaScript del navegador no puede leerla, lo que la protege frente al robo de
 * token por XSS (a diferencia de guardarlo en localStorage).
 *
 * Configurable por entorno:
 *  - auth.cookie.name       (por defecto ACCESS_TOKEN)
 *  - auth.cookie.secure     (por defecto false; en producción DEBE ser true)
 *  - auth.cookie.same-site  (Lax por defecto; en despliegue cross-site usar None)
 * La caducidad se toma de jwt.expiration (ms).
 */
@Component
public class AuthCookies {

    private final String name;
    private final boolean secure;
    private final String sameSite;
    private final long expirationMs;

    public AuthCookies(
            @Value("${auth.cookie.name:ACCESS_TOKEN}") String name,
            @Value("${auth.cookie.secure:false}") boolean secure,
            @Value("${auth.cookie.same-site:Lax}") String sameSite,
            @Value("${jwt.expiration:86400000}") long expirationMs) {
        this.name = name;
        this.secure = secure;
        this.sameSite = sameSite;
        this.expirationMs = expirationMs;
    }

    public String getName() {
        return name;
    }

    public boolean isSecure() {
        return secure;
    }

    public String getSameSite() {
        return sameSite;
    }

    /** Escribe la cookie con el JWT en la respuesta. */
    public void write(HttpServletResponse response, String token) {
        ResponseCookie cookie = baseCookie(token)
                .maxAge(Duration.ofMillis(expirationMs))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** Borra la cookie (logout): mismo nombre/atributos, maxAge 0. */
    public void clear(HttpServletResponse response) {
        ResponseCookie cookie = baseCookie("")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** Extrae el JWT de la cookie de la petición, o null si no está. */
    public String read(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/");
    }
}
