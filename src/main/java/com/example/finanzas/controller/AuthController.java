package com.example.finanzas.controller;

import com.example.finanzas.config.AuthCookies;
import com.example.finanzas.dto.auth.LoginRequest;
import com.example.finanzas.dto.auth.LoginResponse;
import com.example.finanzas.dto.auth.RegisterRequest;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.service.security.JwtService;
import com.example.finanzas.service.security.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthCookies authCookies;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserEntity user = (UserEntity) userService.loadUserByUsername(request.getUsername());
        // El JWT viaja en una cookie httpOnly, no en el cuerpo: así el JS no puede leerlo.
        authCookies.write(response, jwtService.generateToken(user));
        return ResponseEntity.ok(LoginResponse.builder()
                .username(user.getUsername())
                .role(user.getRole().name())
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request,
                                                  HttpServletResponse response) {
        LoginResponse res = userService.register(request);
        authCookies.write(response, res.getToken());
        // No devolvemos el token en el cuerpo; queda solo en la cookie httpOnly.
        res.setToken(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authCookies.clear(response);
        return ResponseEntity.noContent().build();
    }
}
