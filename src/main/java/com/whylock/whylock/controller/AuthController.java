package com.whylock.whylock.controller;

import com.whylock.whylock.model.AuthRequest;
import com.whylock.whylock.model.AuthResponse;
import com.whylock.whylock.model.User;
import com.whylock.whylock.repository.UserRepository;
import com.whylock.whylock.security.JwtUtil;
import com.whylock.whylock.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    // ===========================
    // REGISTER
    // ===========================
    @PostMapping("/register")
    public String register(@RequestBody AuthRequest request) {

        if (repository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username already exists"
            );
        }

        // NEW
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email already exists"
            );
        }

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());     // NEW
        user.setPassword(encoder.encode(request.getPassword()));

        if (request.getRole() == null || request.getRole().isBlank()) {
            user.setRole(User.Role.USER);
        } else {
            try {
                user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid role"
                );
            }
        }

        repository.save(user);

        // Welcome Email
        try {

            emailService.sendWelcomeEmail(
                    user.getEmail(),
                    user.getUsername()
            );

        } catch (Exception e) {

            e.printStackTrace();

        }

        return "User registered successfully";
    }

    // ===========================
    // LOGIN
    // ===========================
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {

        User user = repository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        ));

        if (!encoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid password"
            );
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name()
        );

        return new AuthResponse(token);
    }

}