package com.lgyar.controllers;

import com.lgyar.domain.AppUser;
import com.lgyar.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "user")
public class UserController {

    private final UserRepository repository;

    @RequestMapping(value = "")
    @ResponseBody
    public String index() {
        return "Hello user!";
    }

    @GetMapping(value = "username")
    @ResponseBody
    public String currentUsername(Authentication auth) {
        return "Hello from get username " + auth.getName();
    }

    @GetMapping(value = "users")
    public ResponseEntity<List<AppUser>> getUsers() {
        return ResponseEntity.ok().body(repository.findAll());
    }
}
