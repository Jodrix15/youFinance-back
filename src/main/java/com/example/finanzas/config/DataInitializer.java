package com.example.finanzas.config;

import com.example.finanzas.model.enums.RoleEnum;
import com.example.finanzas.model.UserEntity;
import com.example.finanzas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(UserEntity.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(RoleEnum.ROLE_ADMIN)
                    .build());
        }
        if (!userRepository.existsByUsername("usuario")) {
            userRepository.save(UserEntity.builder()
                    .username("usuario")
                    .password(passwordEncoder.encode("user123"))
                    .role(RoleEnum.ROLE_USER)
                    .build());
        }
    }
}