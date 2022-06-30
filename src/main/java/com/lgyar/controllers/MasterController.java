package com.lgyar.controllers;

import com.lgyar.authentication.MongoUserDetails;
import com.lgyar.domain.User;
import com.lgyar.domain.UserRole;
import com.lgyar.dto.UserDTO;
import com.lgyar.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
public class MasterController {
    @Autowired
    UserRepository repository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping(value = "hello-anon")
    @ResponseBody
    public String getHelloAnon() {
        return "Hello Anon!";
    }

    @GetMapping(value = "hello-user")
    @ResponseBody
    public String getHelloUser() {
        return "Hello user!";
    }

    @GetMapping(value = "hello-admin")
    @ResponseBody
    public String getHelloAdmin() {
        return "Hello admin!";
    }

    @PostMapping(value = "login")
    public ResponseEntity<?> postLogin(@RequestBody UserDTO userDto) {
        Optional<User> retrieved = repository.findById(userDto.getUsername());
        if (retrieved.isPresent()) {
            User user = retrieved.get();
            String userPasswordHash = user.getPasswordHash();
            boolean isPasswordCorrect = passwordEncoder.matches(userDto.getPassword(), userPasswordHash);
            if (isPasswordCorrect) {
                MongoUserDetails userDetails = new MongoUserDetails(user);
                Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("logging user with authorities: ");
                System.out.println(userDetails.getAuthorities());
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
