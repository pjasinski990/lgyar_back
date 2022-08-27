package com.lgyar;

import com.lgyar.domain.AppUser;
import com.lgyar.domain.UserRole;
import com.lgyar.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@SpringBootApplication
public class LikeGivingYourselfARaiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(LikeGivingYourselfARaiseApplication.class, args);
    }

    @Bean
    CommandLineRunner run(UserRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            String rootUsername = "root";
            String rootPassword = "root";
            System.out.println("Creating root user...");
            Optional<AppUser> u = repository.findById(rootUsername);
            if (u.isEmpty()) {
                String passHash = passwordEncoder.encode(rootPassword);
                AppUser user = new AppUser(
                        rootUsername,
                        passHash,
                        UserRole.ROLE_ADMIN,
                        null,
                        null
                );
                repository.deleteAll();
                repository.save(user);
            }
        };
    }
}
