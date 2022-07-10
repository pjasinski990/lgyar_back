package com.lgyar.security;

import com.lgyar.domain.AppUser;
import com.lgyar.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MongoUserDetailService implements UserDetailsService {

    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AppUser> retrieved = repository.findById(username);
        if (retrieved.isPresent()) {
            return new MongoUserDetails(retrieved.get());
        }
        else {
            throw new UsernameNotFoundException(username);
        }
    }
}
