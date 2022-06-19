package com.lgyar.controllers;

import com.lgyar.domain.User;
import com.lgyar.domain.UserRole;
import com.lgyar.dto.UserDTO;
import com.lgyar.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MasterController {
    @Autowired
    UserRepository repository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping(value = "login")
    public ResponseEntity<?> postLogin(@RequestBody UserDTO userDto) {
        Optional<User> retrieved = repository.findById(userDto.getUsername());
        if (retrieved.isPresent()) {
            String retrievedPassHash = retrieved.get().getPasswordHash();
            boolean isPasswordCorrect = passwordEncoder.matches(userDto.getPassword(), retrievedPassHash);
            if (isPasswordCorrect) {
                return ResponseEntity.ok(HttpStatus.OK);
            }
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Incorrect credentials");
    }

    @PostMapping(value = "register")
    public ResponseEntity<HttpStatus> postRegister(@RequestBody UserDTO userDto) {
        String passHash = passwordEncoder.encode(userDto.getPassword());
        User user = new User(
                userDto.getUsername(),
                passHash,
                UserRole.USER,
                null,
                null
        );
        repository.save(user);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
