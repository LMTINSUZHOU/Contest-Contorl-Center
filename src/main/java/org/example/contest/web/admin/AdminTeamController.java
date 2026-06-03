package org.example.contest.web.admin;

import jakarta.validation.Valid;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.example.contest.common.ApiException;
import org.example.contest.domain.Team;
import org.example.contest.domain.TeamMember;
import org.example.contest.repository.StudentProfileRepository;
import org.example.contest.repository.TeamMemberRepository;
import org.example.contest.repository.TeamRepository;
import org.example.contest.web.dto.ManagementDtos;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/teams")
public class AdminTeamController {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final StudentProfileRepository studentRepository;

    public AdminTeamController(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            StudentProfileRepository studentRepository
    ) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public List<TeamView> list() {
        return teamRepository.findAll().stream().map(this::toView).toList();
    }

    @GetMapping("/{id}")
    public TeamView get(@PathVariable UUID id) {
        return toView(team(id));
    }

    @PostMapping
    @Transactional
    public TeamView create(@Valid @RequestBody ManagementDtos.TeamRequest request) {
        teamRepository.findByNameIgnoreCase(request.name()).ifPresent(found -> {
            throw ApiException.badRequest("团队名称已存在");
        });
        Team team = new Team();
        apply(team, request);
        Team saved = teamRepository.save(team);
        replaceMembers(saved, request);
        return toView(saved);
    }

    @PutMapping("/{id}")
    @Transactional
    public TeamView update(@PathVariable UUID id, @Valid @RequestBody ManagementDtos.TeamRequest request) {
        Team team = team(id);
        teamRepository.findByNameIgnoreCase(request.name())
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw ApiException.badRequest("团队名称已存在");
                });
        apply(team, request);
        replaceMembers(team, request);
        return toView(team);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable UUID id) {
        teamRepository.delete(team(id));
    }

    private void apply(Team team, ManagementDtos.TeamRequest request) {
        team.setName(request.name());
        team.setCaptainStudentId(request.captainStudentId());
        team.setNotes(request.notes());
    }

    private void replaceMembers(Team team, ManagementDtos.TeamRequest request) {
        teamMemberRepository.deleteByTeamId(team.getId());
        Set<UUID> memberIds = new LinkedHashSet<>();
        if (request.memberStudentIds() != null) {
            memberIds.addAll(request.memberStudentIds());
        }
        if (request.captainStudentId() != null) {
            memberIds.add(request.captainStudentId());
        }
        if (memberIds.isEmpty()) {
            throw ApiException.badRequest("团队至少需要一个已注册学生成员");
        }
        for (UUID studentId : memberIds) {
            studentRepository.findById(studentId).orElseThrow(() -> ApiException.notFound("团队成员学生不存在"));
            TeamMember member = new TeamMember();
            member.setTeamId(team.getId());
            member.setStudentId(studentId);
            member.setCaptain(studentId.equals(request.captainStudentId()));
            teamMemberRepository.save(member);
        }
    }

    private Team team(UUID id) {
        return teamRepository.findById(id).orElseThrow(() -> ApiException.notFound("团队不存在"));
    }

    private TeamView toView(Team team) {
        List<MemberView> members = teamMemberRepository.findByTeamId(team.getId()).stream()
                .map(member -> studentRepository.findById(member.getStudentId())
                        .map(student -> new MemberView(student.getId(), student.getStudentNo(), student.getName(), member.isCaptain()))
                        .orElse(new MemberView(member.getStudentId(), null, "未知学生", member.isCaptain())))
                .toList();
        return new TeamView(team.getId(), team.getName(), team.getCaptainStudentId(), team.getNotes(), members);
    }

    public record TeamView(UUID id, String name, UUID captainStudentId, String notes, List<MemberView> members) {
    }

    public record MemberView(UUID studentId, String studentNo, String name, boolean captain) {
    }
}
