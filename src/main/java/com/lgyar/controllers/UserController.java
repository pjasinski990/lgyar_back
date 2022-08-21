package com.lgyar.controllers;

import com.lgyar.domain.AppUser;
import com.lgyar.domain.UserRole;
import com.lgyar.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "user")
public class UserController {

    private final UserRepository repository;

    @GetMapping(value = "")
    public ResponseEntity<?> index(Authentication auth) {
        Optional<AppUser> u = repository.findById(auth.getName());
        if (u.isEmpty()) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(u.get());
    }
}
