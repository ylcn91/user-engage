package com.userengage.services;

import com.userengage.models.User;
import com.userengage.repositories.UserRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import jakarta.enterprise.context.ApplicationScoped;

@Slf4j
@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Transactional
    public User createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        userRepository.persist(user);
        log.info("User created: {}", username);
        return user;
    }

    public User findByUsername(String username) {
        return userRepository.find("username", username).firstResult();
    }
}
