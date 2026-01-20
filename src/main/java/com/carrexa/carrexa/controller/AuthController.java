package com.carrexa.carrexa.controller;

import com.carrexa.carrexa.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> body) {
        // Default to "user" if no role is provided
        String role = body.getOrDefault("role", "user");

        return authService.registerUser(
                body.get("username"),
                body.get("password"),
                body.get("email"),
                role // Pass "user", "recruiter", or "admin"
        );
    }
}