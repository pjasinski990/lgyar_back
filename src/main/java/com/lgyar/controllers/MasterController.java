package com.lgyar.controllers;

import com.lgyar.domain.AppUser;
import com.lgyar.domain.UserRole;
import com.lgyar.dto.UserDTO;
import com.lgyar.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class MasterController {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;

    @GetMapping(value = "/hello-anon")
    @ResponseBody
    public String getHelloAnon() {
        return "Hello Anon!";
    }

    @GetMapping(value = "/hello-user")
    @ResponseBody
    public String getHelloUser() {
        return "Hello user!";
    }

    @GetMapping(value = "/hello-admin")
    @ResponseBody
    public String getHelloAdmin() {
        return "Hello admin!";
    }

    @PostMapping(value = "register")
    public ResponseEntity<?> postRegister(@RequestBody UserDTO userDto) {
        Optional<AppUser> u = repository.findById(userDto.getUsername());
        if (u.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }

        String passHash = passwordEncoder.encode(userDto.getPassword());
        AppUser user = new AppUser(
                userDto.getUsername(),
                passHash,
                UserRole.ROLE_USER,
                null,
                null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(user));
    }
}
