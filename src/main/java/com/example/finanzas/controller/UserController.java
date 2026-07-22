package com.example.finanzas.controller;

import com.example.finanzas.dto.user.ChangePasswordRequest;
import com.example.finanzas.dto.user.UpdatePreferencesRequest;
import com.example.finanzas.dto.user.UpdateProfileRequest;
import com.example.finanzas.dto.user.UserProfileResponse;
import com.example.finanzas.service.security.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getUsername()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(principal.getUsername(), request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getUsername(), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/preferences")
    public ResponseEntity<UserProfileResponse> updatePreferences(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        return ResponseEntity.ok(userService.updatePreferences(principal.getUsername(), request));
    }

    /** Config personal del dashboard (disposición/visibilidad de widgets), como JSON en texto. */
    @GetMapping(value = "/me/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDashboard(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userService.getDashboardConfig(principal.getUsername()));
    }

    @PutMapping(value = "/me/dashboard",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateDashboard(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody String config) {
        return ResponseEntity.ok(userService.updateDashboardConfig(principal.getUsername(), config));
    }
}
