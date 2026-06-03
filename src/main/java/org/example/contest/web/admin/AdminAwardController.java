package org.example.contest.web.admin;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.example.contest.domain.enums.AuditStatus;
import org.example.contest.security.UserPrincipal;
import org.example.contest.service.AwardService;
import org.example.contest.web.dto.AuthDtos;
import org.example.contest.web.dto.ManagementDtos;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminAwardController {
    private final AwardService awardService;

    public AdminAwardController(AwardService awardService) {
        this.awardService = awardService;
    }

    @GetMapping("/awards")
    public List<ManagementDtos.AwardView> awards(@RequestParam(required = false) AuditStatus status) {
        return awardService.listAll(status);
    }

    @GetMapping("/awards/{id}")
    public ManagementDtos.AwardView award(@PathVariable UUID id) {
        return awardService.get(id);
    }

    @PostMapping("/awards")
    public ManagementDtos.AwardView create(
            @Valid @RequestBody ManagementDtos.AwardRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return awardService.createByAdmin(request, principal.id());
    }

    @PutMapping("/awards/{id}")
    public ManagementDtos.AwardView update(@PathVariable UUID id, @Valid @RequestBody ManagementDtos.AwardRequest request) {
        return awardService.update(id, request);
    }

    @DeleteMapping("/awards/{id}")
    public void delete(@PathVariable UUID id) {
        awardService.delete(id);
    }

    @GetMapping("/award-declarations")
    public List<ManagementDtos.AwardView> declarations() {
        return awardService.listAll(AuditStatus.PENDING);
    }

    @PostMapping("/award-declarations/{id}/approve")
    public ManagementDtos.AwardView approve(
            @PathVariable UUID id,
            @RequestBody(required = false) AuthDtos.AuditRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return awardService.approve(id, principal.id(), request == null ? null : request.reason());
    }

    @PostMapping("/award-declarations/{id}/reject")
    public ManagementDtos.AwardView reject(
            @PathVariable UUID id,
            @RequestBody(required = false) AuthDtos.AuditRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return awardService.reject(id, principal.id(), request == null ? null : request.reason());
    }
}
