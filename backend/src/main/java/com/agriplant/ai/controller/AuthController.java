package com.agriplant.ai.controller;

import com.agriplant.ai.model.User;
import com.agriplant.ai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email and password are required"));
        }

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null || !user.getPasswordHash().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "userId", user.getUserId(),
                "name", user.getFullName()
        ));
    }
}
