package com.example.racketbackend.services;

import com.example.racketbackend.models.User;
import com.example.racketbackend.models.UserRole;
import com.example.racketbackend.repositories.UserRepository;
import com.example.racketbackend.security.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserService {


    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public ResponseEntity<Object> getUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }


    public ResponseEntity<Object> getUserById(Integer id) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404)
                    .body("User not found");
        }

        return ResponseEntity.ok(user);
    }
    public ResponseEntity<Object> register(User user) {

        if (user.getEmail() == null ||
                user.getEmail().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Email cannot be empty");
        }

        if (user.getPassword() == null ||
                user.getPassword().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Password cannot be empty");
        }

        if (userRepository.findByEmail(user.getEmail())
                .isPresent()) {

            return ResponseEntity.badRequest()
                    .body("Email already exists");
        }

        user.setRole(UserRole.USER);
        user.setActive(true);

        // Encrypt the password before saving
        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        User savedUser = userRepository.save(user);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", savedUser.getId());
        response.put("name", savedUser.getName());
        response.put("email", savedUser.getEmail());
        response.put("role", savedUser.getRole());

        return ResponseEntity.ok(response);
    }



    public ResponseEntity<Object> login(User user) {

        if (user.getEmail() == null ||
                user.getPassword() == null) {

            return ResponseEntity.badRequest()
                    .body("Email and password are required");
        }

        User existingUser = userRepository
                .findByEmail(user.getEmail())
                .orElse(null);

        if (existingUser == null) {
            return ResponseEntity.status(401)
                    .body("Invalid email or password");
        }

        if (!passwordEncoder.matches(
                user.getPassword(),
                existingUser.getPassword())) {

            return ResponseEntity.status(401)
                    .body("Invalid email or password");
        }

        if (!Boolean.TRUE.equals(existingUser.getActive())) {
            return ResponseEntity.status(403)
                    .body("This account has been disabled");
        }

        String token = jwtService.generateToken(existingUser);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", existingUser.getId());
        response.put("name", existingUser.getName());
        response.put("email", existingUser.getEmail());
        response.put("role", existingUser.getRole());
        response.put("token", token);

        return ResponseEntity.ok(response);



    }


    public ResponseEntity<Object> disableUser(Integer id) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404)
                    .body("User not found");
        }

        user.setActive(false);
        userRepository.save(user);

        return ResponseEntity.ok("User disabled successfully");
    }

    public ResponseEntity<Object> makeUserAdmin(
            Integer userId) {

        User user = userRepository
                .findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(404)
                    .body("User not found");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            return ResponseEntity.badRequest()
                    .body("Disabled user cannot become admin");
        }

        if (user.getRole() == UserRole.ADMIN) {
            return ResponseEntity.badRequest()
                    .body("User is already an admin");
        }

        user.setRole(UserRole.ADMIN);
        userRepository.save(user);

        return ResponseEntity.ok(
                "User promoted to admin successfully"
        );
    }



}