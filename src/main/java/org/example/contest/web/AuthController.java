package org.example.contest.web;

import jakarta.validation.Valid;
import org.example.contest.security.UserPrincipal;
import org.example.contest.service.AccountService;
import org.example.contest.web.dto.AuthDtos;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register/student")
    public AuthDtos.UserView registerStudent(@Valid @RequestBody AuthDtos.RegisterStudentRequest request) {
        return accountService.registerStudent(request);
    }

    @PostMapping("/register/teacher")
    public AuthDtos.UserView registerTeacher(@Valid @RequestBody AuthDtos.RegisterTeacherRequest request) {
        return accountService.registerTeacher(request);
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return accountService.login(request);
    }

    @GetMapping("/me")
    public AuthDtos.UserView me(@AuthenticationPrincipal UserPrincipal principal) {
        return accountService.me(principal.id());
    }
}
