package com.lgyar.controllers;

import com.lgyar.domain.AppUser;
import com.lgyar.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "admin")
public class AdminController {

    private final UserRepository repository;

    @GetMapping(value = "users")
    public ResponseEntity<List<AppUser>> getUsers() {
        return ResponseEntity.ok().body(repository.findAll());
    }
}
