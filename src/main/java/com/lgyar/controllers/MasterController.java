package com.lgyar.controllers;

import com.lgyar.domain.User;
import com.lgyar.domain.UserRole;
import com.lgyar.dto.UserDTO;
import com.lgyar.repositories.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

@RestController
public class MasterController {

    public MasterController(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder, PasswordEncoder passwordEncoder, UserRepository repository) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

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

/*
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
*/
    @PostMapping(value = "register")
    public ResponseEntity<HttpStatus> postRegister(@RequestBody UserDTO userDto) {
        String passHash = passwordEncoder.encode(userDto.getPassword());
        User user = new User(
                userDto.getUsername(),
                passHash,
                UserRole.ROLE_USER,
                null,
                null
        );
        repository.save(user);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("login")
    public ResponseEntity<String> login(@RequestBody UserDTO userDTO) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword()));

            Instant now = Instant.now();
            final long expiry = 36000L;

            String scope = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(joining(" "));

            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(expiry))
                    .subject(format("%s", userDTO.getUsername()))
                    .claim("roles", scope)
                    .build();

            String token = this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .body("Temp body, token type Bearer");
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;

}
