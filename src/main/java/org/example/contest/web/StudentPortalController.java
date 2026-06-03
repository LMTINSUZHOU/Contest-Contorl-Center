package org.example.contest.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.example.contest.common.ApiException;
import org.example.contest.domain.StudentProfile;
import org.example.contest.repository.StudentProfileRepository;
import org.example.contest.security.UserPrincipal;
import org.example.contest.service.AwardService;
import org.example.contest.web.dto.ManagementDtos;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentPortalController {
    private final StudentProfileRepository studentRepository;
    private final AwardService awardService;

    public StudentPortalController(StudentProfileRepository studentRepository, AwardService awardService) {
        this.studentRepository = studentRepository;
        this.awardService = awardService;
    }

    @GetMapping("/profile")
    public StudentProfile profile(@AuthenticationPrincipal UserPrincipal principal) {
        return studentRepository.findByUserId(principal.id()).orElseThrow(() -> ApiException.notFound("学生资料不存在"));
    }

    @GetMapping("/awards")
    public List<ManagementDtos.AwardView> awards(@AuthenticationPrincipal UserPrincipal principal) {
        return awardService.studentAwards(principal.id());
    }

    @GetMapping("/award-declarations")
    public List<ManagementDtos.AwardView> declarations(@AuthenticationPrincipal UserPrincipal principal) {
        return awardService.studentDeclarations(principal.id());
    }

    @PostMapping("/award-declarations")
    public ManagementDtos.AwardView declareAward(
            @Valid @RequestBody ManagementDtos.AwardDeclarationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return awardService.declareByStudent(request, principal.id());
    }

    @PutMapping("/award-declarations/{id}")
    public ManagementDtos.AwardView updateAward(
            @PathVariable UUID id,
            @Valid @RequestBody ManagementDtos.AwardDeclarationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return awardService.updateByStudent(id, request, principal.id());
    }

    @DeleteMapping("/award-declarations/{id}")
    public void deleteAward(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        awardService.deleteByStudent(id, principal.id());
    }
}
