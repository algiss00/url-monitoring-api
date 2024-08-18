package com.applifting.code_assessment.services;

import com.applifting.code_assessment.domain.User;
import com.applifting.code_assessment.exceptions.UnauthorizedException;
import com.applifting.code_assessment.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @Autowired
    public UserService(UserRepository userRepository, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    public User getUserByAccessToken(String accessToken) {
        return userRepository.findByAccessToken(accessToken)
                .orElseThrow(() ->
                        new UnauthorizedException(messageSource.getMessage("unauthorized.access", null, LocaleContextHolder.getLocale())));
    }

    public User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        return userRepository.save(user);
    }

}