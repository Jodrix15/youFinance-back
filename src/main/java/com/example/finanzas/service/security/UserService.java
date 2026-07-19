package com.example.finanzas.service.security;

import com.example.finanzas.dto.auth.LoginResponse;
import com.example.finanzas.dto.auth.RegisterRequest;
import com.example.finanzas.dto.user.ChangePasswordRequest;
import com.example.finanzas.dto.user.UpdatePreferencesRequest;
import com.example.finanzas.dto.user.UpdateProfileRequest;
import com.example.finanzas.dto.user.UserProfileResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    LoginResponse register(RegisterRequest request);

    UserProfileResponse getProfile(String username);

    UserProfileResponse updateProfile(String username, UpdateProfileRequest request);

    void changePassword(String username, ChangePasswordRequest request);

    UserProfileResponse updatePreferences(String username, UpdatePreferencesRequest request);
}
