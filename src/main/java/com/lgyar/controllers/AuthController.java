package com.lgyar.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lgyar.domain.AppUser;
import com.lgyar.domain.UserRole;
import com.lgyar.repositories.UserRepository;
import com.lgyar.security.RSAKeyLoader;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@RestController
public class AuthController {

    public AuthController(PasswordEncoder passwordEncoder, UserRepository repository) {
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
        try {
            this.rsaPublicKey = RSAKeyLoader.readPublicKey(new File("src/main/resources/id_rsa.pub"));
            this.rsaPrivateKey = RSAKeyLoader.readPrivateKey(new File("src/main/resources/id_rsa"));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final RSAPublicKey rsaPublicKey;
    private final RSAPrivateKey rsaPrivateKey;

    @Data
    @AllArgsConstructor
    private static class UserRegisterResponse {
        private String error_message;
        private AppUser created;
    }

    @PostMapping(value = "register")
    public UserRegisterResponse postRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Optional<AppUser> u = repository.findById(username);

        if (u.isPresent()) {
            response.setStatus(HttpStatus.CONFLICT.value());
            return new UserRegisterResponse("User already exists", null);
        }
        else {
            String passHash = passwordEncoder.encode(password);
            AppUser user = new AppUser(
                    username,
                    passHash,
                    UserRole.ROLE_USER,
                    null,
                    null
            );
            AppUser resUser = repository.save(user);
            response.setStatus(HttpStatus.CREATED.value());
            return new UserRegisterResponse("", resUser);
        }
    }

    @GetMapping(value = "refresh_token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                String refreshToken = authorization.substring(("Bearer ".length()));
                Algorithm algorithm = Algorithm.RSA256(rsaPublicKey, rsaPrivateKey);
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refreshToken);
                String username = decodedJWT.getSubject();

                Optional<AppUser> u = repository.findById(username);
                if (u.isPresent()) {
                    AppUser user = u.get();
                    String accessToken = JWT.create()
                            .withSubject(username)
                            .withExpiresAt(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                            .withIssuer(request.getRequestURL().toString())
                            .withClaim("roles", List.of(user.getRole().toString()))
                            .sign(algorithm);

                    Map<String, String> body = new HashMap<>();
                    body.put("access_token", accessToken);
                    body.put("refresh_token", refreshToken);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), body);
                }
            } catch (Exception e) {
                Map<String, String> body = new HashMap<>();
                body.put("error_message", e.getMessage());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(HttpStatus.FORBIDDEN.value());
                new ObjectMapper().writeValue(response.getOutputStream(), body);
            }
        }
        else {
            throw new RuntimeException("Refresh token missing");
        }
    }
}
