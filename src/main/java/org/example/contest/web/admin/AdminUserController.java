package org.example.contest.web.admin;

import java.util.List;
import java.util.UUID;
import org.example.contest.security.UserPrincipal;
import org.example.contest.service.AccountService;
import org.example.contest.web.dto.AuthDtos;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AccountService accountService;

    public AdminUserController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/pending")
    public List<AuthDtos.UserView> pending() {
        return accountService.pendingUsers();
    }

    @PostMapping("/{id}/approve")
    public AuthDtos.UserView approve(
            @PathVariable UUID id,
            @RequestBody(required = false) AuthDtos.AuditRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return accountService.approve(id, principal.id(), request == null ? null : request.reason());
    }

    @PostMapping("/{id}/reject")
    public AuthDtos.UserView reject(
            @PathVariable UUID id,
            @RequestBody(required = false) AuthDtos.AuditRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return accountService.reject(id, principal.id(), request == null ? null : request.reason());
    }

    @PostMapping("/{id}/disable")
    public AuthDtos.UserView disable(
            @PathVariable UUID id,
            @RequestBody(required = false) AuthDtos.AuditRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return accountService.disable(id, principal.id(), request == null ? null : request.reason());
    }
}
