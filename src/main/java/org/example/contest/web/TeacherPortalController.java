package org.example.contest.web;

import java.util.List;
import java.util.UUID;
import org.example.contest.common.ApiException;
import org.example.contest.domain.TeacherProfile;
import org.example.contest.repository.TeacherProfileRepository;
import org.example.contest.security.UserPrincipal;
import org.example.contest.service.AwardService;
import org.example.contest.web.dto.AuthDtos;
import org.example.contest.web.dto.ManagementDtos;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher")
public class TeacherPortalController {
    private final TeacherProfileRepository teacherRepository;
    private final AwardService awardService;

    public TeacherPortalController(TeacherProfileRepository teacherRepository, AwardService awardService) {
        this.teacherRepository = teacherRepository;
        this.awardService = awardService;
    }

    @GetMapping("/profile")
    public TeacherProfile profile(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherRepository.findByUserId(principal.id()).orElseThrow(() -> ApiException.notFound("教师资料不存在"));
    }

    @GetMapping("/awards")
    public List<ManagementDtos.AwardView> awards(@AuthenticationPrincipal UserPrincipal principal) {
        return awardService.teacherAwards(principal.id());
    }

    @GetMapping("/guidance")
    public List<ManagementDtos.AwardView> guidance(@AuthenticationPrincipal UserPrincipal principal) {
        return awardService.teacherAwards(principal.id());
    }

    @GetMapping("/award-declarations")
    public List<ManagementDtos.AwardView> declarations(@AuthenticationPrincipal UserPrincipal principal) {
        return awardService.teacherPendingDeclarations(principal.id());
    }

    @PostMapping("/award-declarations/{id}/approve")
    public ManagementDtos.AwardView approve(
            @PathVariable UUID id,
            @RequestBody(required = false) AuthDtos.AuditRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return awardService.approveByTeacher(id, principal.id(), request == null ? null : request.reason());
    }

    @PostMapping("/award-declarations/{id}/reject")
    public ManagementDtos.AwardView reject(
            @PathVariable UUID id,
            @RequestBody(required = false) AuthDtos.AuditRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return awardService.rejectByTeacher(id, principal.id(), request == null ? null : request.reason());
    }
}
