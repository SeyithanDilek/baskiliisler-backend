package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public User getCurrentUser() {
        return userService.getCurrentUser();
    }
} 