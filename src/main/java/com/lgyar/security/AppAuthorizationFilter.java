package com.lgyar.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

public class AppAuthorizationFilter extends OncePerRequestFilter {

    public AppAuthorizationFilter() {
        try {
            this.rsaPublicKey = RSAKeyLoader.readPublicKey(new File("src/main/resources/id_rsa.pub"));
            this.rsaPrivateKey = RSAKeyLoader.readPrivateKey(new File("src/main/resources/id_rsa"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final RSAPublicKey rsaPublicKey;
    private final RSAPrivateKey rsaPrivateKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!request.getServletPath().equals("/api/login")) {
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null && authorization.startsWith("Bearer ")) {
                try {
                    String token = authorization.substring(("Bearer ".length()));
                    Algorithm algorithm = Algorithm.RSA256(rsaPublicKey, rsaPrivateKey);
                    JWTVerifier verifier = JWT.require(algorithm).build();
                    DecodedJWT decodedJWT = verifier.verify(token);
                    String username = decodedJWT.getSubject();

                    String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    Arrays.stream(roles).forEach(role -> {
                        authorities.add(new SimpleGrantedAuthority(role));}
                    );

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    filterChain.doFilter(request, response);

                } catch (Exception e) {
                    Map<String, String> body = new HashMap<>();
                    body.put("error_message", e.getMessage());
                    new ObjectMapper().writeValue(response.getOutputStream(), body);

                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                }
            }
            else {
                Map<String, String> body = new HashMap<>();
                body.put("error_message", "You need to log in first");
                new ObjectMapper().writeValue(response.getOutputStream(), body);

                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(HttpStatus.FORBIDDEN.value());
            }
        }
        else {
            filterChain.doFilter(request, response);
        }
    }
}
