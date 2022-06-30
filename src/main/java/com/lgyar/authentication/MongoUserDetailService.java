package com.lgyar.authentication;

import com.lgyar.domain.User;
import com.lgyar.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MongoUserDetailService implements UserDetailsService {

    public MongoUserDetailService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> retrieved = repository.findById(username);
        if (retrieved.isPresent()) {
            return new MongoUserDetails(retrieved.get());
        }
        else {
            throw new UsernameNotFoundException(username);
        }
    }

    private final UserRepository repository;
}
