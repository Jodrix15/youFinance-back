package com.example.finanzas.service.impl.securityImpl;
import com.example.finanzas.service.security.UserService;
import com.example.finanzas.service.security.JwtService;

import com.example.finanzas.dto.auth.LoginResponse;
import com.example.finanzas.dto.auth.RegisterRequest;
import com.example.finanzas.dto.user.ChangePasswordRequest;
import com.example.finanzas.dto.user.UpdatePreferencesRequest;
import com.example.finanzas.dto.user.UpdateProfileRequest;
import com.example.finanzas.dto.user.UserProfileResponse;
import com.example.finanzas.model.enums.RoleEnum;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }
        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleEnum.ROLE_USER)
                .build();
        userRepository.save(user);
        return LoginResponse.builder()
                .token(jwtService.generateToken(user))
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String username) {
        return toResponse(findUser(username), null);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        UserEntity user = findUser(username);

        String nuevoUsername = request.getUsername().trim();
        boolean usernameCambiado = !nuevoUsername.equals(user.getUsername());
        if (usernameCambiado && userRepository.existsByUsername(nuevoUsername)) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        String nuevoEmail = StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null;
        boolean emailCambiado = nuevoEmail != null && !nuevoEmail.equalsIgnoreCase(user.getEmail());
        if (emailCambiado && userRepository.existsByEmail(nuevoEmail)) {
            throw new IllegalArgumentException("El email ya está en uso");
        }

        user.setUsername(nuevoUsername);
        user.setEmail(nuevoEmail);
        // "" desde el front significa eliminar la foto.
        user.setFotoPerfil(StringUtils.hasText(request.getFotoPerfil()) ? request.getFotoPerfil() : null);
        userRepository.save(user);

        // Si cambió el username, el JWT anterior (subject = username) deja de ser válido:
        // emitimos uno nuevo para que el front no pierda la sesión.
        String token = usernameCambiado ? jwtService.generateToken(user) : null;
        return toResponse(user, token);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        UserEntity user = findUser(username);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser distinta de la actual");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updatePreferences(String username, UpdatePreferencesRequest request) {
        UserEntity user = findUser(username);
        user.setMoneda(request.getMoneda());
        user.setIdioma(request.getIdioma());
        userRepository.save(user);
        return toResponse(user, null);
    }

    private UserEntity findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    private UserProfileResponse toResponse(UserEntity user, String token) {
        return UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fotoPerfil(user.getFotoPerfil())
                .moneda(user.getMoneda())
                .idioma(user.getIdioma())
                .token(token)
                .build();
    }
}
