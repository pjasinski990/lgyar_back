package com.lgyar.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class AppAuthorizationFilter extends OncePerRequestFilter {

    public AppAuthorizationFilter() {
        try {
            this.rsaPublicKey = KeyLoader.readPublicKey(new File("src/main/resources/id_rsa.pub"));
            this.rsaPrivateKey = KeyLoader.readPrivateKey(new File("src/main/resources/id_rsa"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final RSAPublicKey rsaPublicKey;
    private final RSAPrivateKey rsaPrivateKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!request.getServletPath().equals("/login")) {
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
                    // TODO change this to send in json body instead of header
                    response.setHeader("message", "Error logging in: " + e.getMessage());
                    response.sendError(HttpStatus.FORBIDDEN.value());
                }
            }
            else {
                response.setHeader("message", "You need to log in first");
                response.sendRedirect("/login");
            }
        }
        else {
            filterChain.doFilter(request, response);
        }
    }
}
