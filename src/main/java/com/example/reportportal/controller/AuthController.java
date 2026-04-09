package com.example.reportportal.controller;

import com.example.reportportal.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("http://localhost:3000")
public class AuthController {

    private final JwtUtil jwtUtil;

    // Hardcoded demo users: username -> {password, role}
    private static final Map<String, String[]> USERS = Map.of(
            "admin", new String[]{"admin123", "Admin"},
            "support", new String[]{"support123", "L2 Support"},
            "l3", new String[]{"debug123", "L3 Engineer"}
    );

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        String[] userInfo = USERS.get(username);
        if (userInfo == null || !userInfo[0].equals(password)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtUtil.generateToken(username);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("token", token);
        response.put("username", username);
        response.put("role", userInfo[1]);

        return ResponseEntity.ok(response);
    }
}
