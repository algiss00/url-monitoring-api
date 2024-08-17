package com.applifting.code_assessment.services;

import com.applifting.code_assessment.domain.User;
import com.applifting.code_assessment.exceptions.UnauthorizedException;
import com.applifting.code_assessment.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByAccessToken(String accessToken) {
        return userRepository.findByAccessToken(accessToken);
    }

    public User getUserByAccessToken(String accessToken) {
        return userRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid access token"));
    }

    public User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        return userRepository.save(user);
    }

}