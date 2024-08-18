package com.applifting.code_assessment.controllers;

import com.applifting.code_assessment.domain.User;
import com.applifting.code_assessment.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/monitoring-app/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<User> getUserDetails(@RequestHeader("Authorization") String accessToken) {
        User user = userService.getUserByAccessToken(accessToken);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        User newUser = userService.createUser(user.getUsername(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

}
