package org.example.contest.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.example.contest.common.ApiException;
import org.example.contest.domain.Award;
import org.example.contest.domain.AwardAdvisor;
import org.example.contest.domain.CertificateFile;
import org.example.contest.domain.ReviewLog;
import org.example.contest.domain.StudentProfile;
import org.example.contest.domain.Team;
import org.example.contest.domain.TeamMember;
import org.example.contest.domain.enums.AuditStatus;
import org.example.contest.domain.enums.AwardSubjectType;
import org.example.contest.repository.AwardAdvisorRepository;
import org.example.contest.repository.AwardRepository;
import org.example.contest.repository.CertificateFileRepository;
import org.example.contest.repository.ReviewLogRepository;
import org.example.contest.repository.StudentProfileRepository;
import org.example.contest.repository.TeamMemberRepository;
import org.example.contest.repository.TeamRepository;
import org.example.contest.repository.TeacherProfileRepository;
import org.example.contest.web.dto.ManagementDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AwardService {
    private final AwardRepository awardRepository;
    private final AwardAdvisorRepository advisorRepository;
    private final CertificateFileRepository certificateRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final StudentProfileRepository studentRepository;
    private final TeacherProfileRepository teacherRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final DirectoryService directoryService;

    public AwardService(
            AwardRepository awardRepository,
            AwardAdvisorRepository advisorRepository,
            CertificateFileRepository certificateRepository,
            ReviewLogRepository reviewLogRepository,
            StudentProfileRepository studentRepository,
            TeacherProfileRepository teacherRepository,
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            DirectoryService directoryService
    ) {
        this.awardRepository = awardRepository;
        this.advisorRepository = advisorRepository;
        this.certificateRepository = certificateRepository;
        this.reviewLogRepository = reviewLogRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.directoryService = directoryService;
    }

    public List<ManagementDtos.AwardView> listAll(AuditStatus status) {
        List<Award> awards = status == null ? awardRepository.findAll() : awardRepository.findByAuditStatus(status);
        return awards.stream().map(this::toView).toList();
    }

    public ManagementDtos.AwardView get(UUID id) {
        return toView(load(id));
    }

    @Transactional
    public ManagementDtos.AwardView createByAdmin(ManagementDtos.AwardRequest request, UUID adminUserId) {
        Award award = fillAward(new Award(), request);
        award.setAuditStatus(request.auditStatus() == null ? AuditStatus.APPROVED : request.auditStatus());
        award.setAuditOpinion(request.auditOpinion());
        award.setCreatedByUserId(adminUserId);
        Award saved = awardRepository.save(award);
        replaceAdvisors(saved.getId(), request.advisorTeacherIds());
        if (saved.getAuditStatus() == AuditStatus.APPROVED) {
            writeReviewLog(saved.getId(), "APPROVE", "管理员直接录入", adminUserId);
        }
        return toView(saved);
    }

    @Transactional
    public ManagementDtos.AwardView update(UUID id, ManagementDtos.AwardRequest request) {
        Award award = fillAward(load(id), request);
        if (request.auditStatus() != null) {
            award.setAuditStatus(request.auditStatus());
        }
        award.setAuditOpinion(request.auditOpinion());
        replaceAdvisors(award.getId(), request.advisorTeacherIds());
        return toView(award);
    }

    @Transactional
    public ManagementDtos.AwardView updateByStudent(UUID id, ManagementDtos.AwardDeclarationRequest request, UUID studentUserId) {
        StudentProfile student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> ApiException.notFound("学生资料不存在"));
        Award award = load(id);
        ensureStudentCanManageAward(student, award);
        applyStudentDeclaration(award, request, student, studentUserId);
        award.setAuditStatus(AuditStatus.PENDING);
        award.setAuditOpinion(null);
        award.setDeclaredByUserId(studentUserId);
        replaceAdvisors(award.getId(), request.advisorTeacherIds());
        writeReviewLog(award.getId(), "UPDATE_SUBMIT", "学生更新后重新提交审核", studentUserId);
        return toView(award);
    }

    @Transactional
    public void delete(UUID id) {
        awardRepository.delete(load(id));
    }

    @Transactional
    public void deleteByStudent(UUID id, UUID studentUserId) {
        StudentProfile student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> ApiException.notFound("学生资料不存在"));
        Award award = load(id);
        ensureStudentCanManageAward(student, award);
        awardRepository.delete(award);
    }

    @Transactional
    public ManagementDtos.AwardView declareByStudent(ManagementDtos.AwardDeclarationRequest request, UUID studentUserId) {
        StudentProfile student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> ApiException.notFound("学生资料不存在"));
        Award award = new Award();
        applyStudentDeclaration(award, request, student, studentUserId);
        award.setAuditStatus(AuditStatus.PENDING);
        Award saved = awardRepository.save(award);
        replaceAdvisors(saved.getId(), request.advisorTeacherIds());
        return toView(saved);
    }

    public List<ManagementDtos.AwardView> studentAwards(UUID studentUserId) {
        StudentProfile student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> ApiException.notFound("学生资料不存在"));
        Set<UUID> teamIds = new HashSet<>(teamMemberRepository.findByStudentId(student.getId()).stream()
                .map(TeamMember::getTeamId)
                .toList());
        return awardRepository.findByAuditStatus(AuditStatus.APPROVED).stream()
                .filter(award -> student.getId().equals(award.getPrimaryStudentId())
                        || (award.getTeamId() != null && teamIds.contains(award.getTeamId())))
                .map(this::toView)
                .toList();
    }

    public List<ManagementDtos.AwardView> studentDeclarations(UUID studentUserId) {
        StudentProfile student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> ApiException.notFound("学生资料不存在"));
        return awardRepository.findAll().stream()
                .filter(award -> canStudentManageAward(student, award))
                .map(this::toView)
                .toList();
    }

    public List<ManagementDtos.AwardView> teacherAwards(UUID teacherUserId) {
        UUID teacherId = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> ApiException.notFound("教师资料不存在"))
                .getId();
        Set<UUID> advisedAwardIds = advisorRepository.findByTeacherId(teacherId).stream()
                .map(AwardAdvisor::getAwardId)
                .collect(java.util.stream.Collectors.toSet());
        return awardRepository.findByAuditStatus(AuditStatus.APPROVED).stream()
                .filter(award -> teacherId.equals(award.getTeacherSubjectId()) || advisedAwardIds.contains(award.getId()))
                .map(this::toView)
                .toList();
    }

    public List<ManagementDtos.AwardView> teacherPendingDeclarations(UUID teacherUserId) {
        UUID teacherId = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> ApiException.notFound("教师资料不存在"))
                .getId();
        Set<UUID> advisedAwardIds = advisorRepository.findByTeacherId(teacherId).stream()
                .map(AwardAdvisor::getAwardId)
                .collect(java.util.stream.Collectors.toSet());
        return awardRepository.findByAuditStatus(AuditStatus.PENDING).stream()
                .filter(award -> advisedAwardIds.contains(award.getId()))
                .map(this::toView)
                .toList();
    }

    @Transactional
    public ManagementDtos.AwardView approve(UUID awardId, UUID reviewerId, String opinion) {
        Award award = load(awardId);
        award.setAuditStatus(AuditStatus.APPROVED);
        award.setAuditOpinion(opinion);
        writeReviewLog(awardId, "APPROVE", opinion, reviewerId);
        return toView(award);
    }

    @Transactional
    public ManagementDtos.AwardView approveByTeacher(UUID awardId, UUID teacherUserId, String opinion) {
        UUID teacherId = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> ApiException.notFound("教师资料不存在"))
                .getId();
        ensureTeacherCanReviewAward(teacherId, awardId);
        Award award = load(awardId);
        award.setAuditStatus(AuditStatus.APPROVED);
        award.setAuditOpinion(opinion);
        writeReviewLog(awardId, "TEACHER_APPROVE", opinion, teacherUserId);
        return toView(award);
    }

    @Transactional
    public ManagementDtos.AwardView reject(UUID awardId, UUID reviewerId, String opinion) {
        Award award = load(awardId);
        award.setAuditStatus(AuditStatus.REJECTED);
        award.setAuditOpinion(opinion);
        writeReviewLog(awardId, "REJECT", opinion, reviewerId);
        return toView(award);
    }

    @Transactional
    public ManagementDtos.AwardView rejectByTeacher(UUID awardId, UUID teacherUserId, String opinion) {
        UUID teacherId = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> ApiException.notFound("教师资料不存在"))
                .getId();
        ensureTeacherCanReviewAward(teacherId, awardId);
        Award award = load(awardId);
        award.setAuditStatus(AuditStatus.REJECTED);
        award.setAuditOpinion(opinion);
        writeReviewLog(awardId, "TEACHER_REJECT", opinion, teacherUserId);
        return toView(award);
    }

    public boolean canUserAccessAward(UUID userId, org.example.contest.domain.enums.UserRole role, UUID awardId) {
        Award award = load(awardId);
        if (role == org.example.contest.domain.enums.UserRole.ADMIN) {
            return true;
        }
        if (award.getAuditStatus() != AuditStatus.APPROVED) {
            return false;
        }
        if (role == org.example.contest.domain.enums.UserRole.STUDENT) {
            StudentProfile student = studentRepository.findByUserId(userId).orElse(null);
            if (student == null) {
                return false;
            }
            if (student.getId().equals(award.getPrimaryStudentId())) {
                return true;
            }
            return award.getTeamId() != null && teamMemberRepository.findByStudentId(student.getId()).stream()
                    .anyMatch(member -> award.getTeamId().equals(member.getTeamId()));
        }
        if (role == org.example.contest.domain.enums.UserRole.TEACHER) {
            UUID teacherId = teacherRepository.findByUserId(userId).map(t -> t.getId()).orElse(null);
            if (teacherId == null) {
                return false;
            }
            return teacherId.equals(award.getTeacherSubjectId()) || advisorRepository.findByAwardId(awardId).stream()
                    .anyMatch(advisor -> teacherId.equals(advisor.getTeacherId()));
        }
        return false;
    }

    public boolean canUserUploadCertificate(UUID userId, org.example.contest.domain.enums.UserRole role, UUID awardId) {
        Award award = load(awardId);
        if (role == org.example.contest.domain.enums.UserRole.ADMIN || userId.equals(award.getDeclaredByUserId())) {
            return true;
        }
        if (role == org.example.contest.domain.enums.UserRole.STUDENT) {
            StudentProfile student = studentRepository.findByUserId(userId).orElse(null);
            if (student == null) {
                return false;
            }
            return student.getId().equals(award.getPrimaryStudentId());
        }
        return false;
    }

    public Award load(UUID id) {
        return awardRepository.findById(id).orElseThrow(() -> ApiException.notFound("获奖记录不存在"));
    }

    public ManagementDtos.AwardView toView(Award award) {
        List<AwardAdvisor> advisors = advisorRepository.findByAwardId(award.getId());
        List<UUID> advisorIds = advisors.stream()
                .map(AwardAdvisor::getTeacherId)
                .toList();
        List<String> advisorNames = advisorIds.stream()
                .map(directoryService::teacherName)
                .filter(name -> name != null && !name.isBlank())
                .toList();
        UUID certificateId = certificateRepository.findFirstByAwardIdAndActiveTrueOrderByCreatedAtDesc(award.getId())
                .map(CertificateFile::getId)
                .orElse(null);
        return new ManagementDtos.AwardView(
                award.getId(),
                award.getCompetitionId(),
                directoryService.competitionName(award.getCompetitionId()),
                award.getCompetitionAlias(),
                award.getTrackId(),
                directoryService.trackName(award.getTrackId()),
                award.getCompetitionGrade(),
                award.getCompetitionGrade().getLabel(),
                award.getAwardLevel(),
                award.getAwardLevel().getLabel(),
                award.getSubjectType(),
                award.getSubjectType().getLabel(),
                award.getAwardYear(),
                award.getAwardDate(),
                award.getAwardLocation(),
                award.getPrimaryStudentId(),
                directoryService.studentName(award.getPrimaryStudentId()),
                award.getTeacherSubjectId(),
                directoryService.teacherName(award.getTeacherSubjectId()),
                award.getTeamId(),
                award.getTeamName(),
                advisorIds,
                advisorNames,
                award.getAuditStatus(),
                award.getAuditStatus().getLabel(),
                award.getAuditOpinion(),
                certificateId
        );
    }

    private Award fillAward(Award award, ManagementDtos.AwardRequest request) {
        applyTrackSelection(award, request.competitionId(), request.trackId());
        award.setCompetitionAlias(request.competitionAlias());
        award.setAwardLevel(request.awardLevel());
        award.setSubjectType(request.subjectType());
        award.setAwardDate(request.awardDate());
        award.setAwardYear(yearOf(request.awardDate()));
        award.setAwardLocation(request.awardLocation());
        award.setPrimaryStudentId(request.primaryStudentId());
        award.setTeacherSubjectId(request.teacherSubjectId());
        award.setTeamId(request.teamId());
        award.setTeamName(resolveTeamName(request.teamId()));
        award.setTeamAward(request.subjectType() == AwardSubjectType.TEAM);
        award.setTeacherAwardName(request.teacherAwardName());
        normalizeSubjectFields(award);
        validateSubject(award);
        return award;
    }

    private void applyTrackSelection(Award award, UUID competitionId, UUID trackId) {
        directoryService.requireCompetition(competitionId);
        directoryService.requireTrack(competitionId, trackId);
        award.setCompetitionId(competitionId);
        award.setLevelId(null);
        award.setTrackId(trackId);
        award.setItemId(null);
        award.setCompetitionGrade(directoryService.resolveCompetitionGrade(competitionId));
    }

    private void applyStudentDeclaration(Award award, ManagementDtos.AwardDeclarationRequest request, StudentProfile student, UUID studentUserId) {
        applyTrackSelection(award, request.competitionId(), request.trackId());
        award.setCompetitionAlias(request.competitionAlias());
        award.setAwardLevel(request.awardLevel());
        award.setAwardDate(request.awardDate());
        award.setAwardYear(yearOf(request.awardDate()));
        award.setAwardLocation(request.awardLocation());
        award.setDeclaredByUserId(studentUserId);
        if (award.getCreatedByUserId() == null) {
            award.setCreatedByUserId(studentUserId);
        }
        boolean teamAward = Boolean.TRUE.equals(request.teamAward()) || request.teamId() != null;
        if (teamAward) {
            award.setSubjectType(AwardSubjectType.TEAM);
            award.setTeamAward(true);
            award.setTeamId(request.teamId());
            award.setTeamName(resolveTeamName(request.teamId()));
            ensureStudentInTeam(student.getId(), request.teamId());
            award.setPrimaryStudentId(null);
            award.setTeacherSubjectId(null);
        } else {
            award.setSubjectType(AwardSubjectType.STUDENT);
            award.setTeamAward(false);
            award.setPrimaryStudentId(student.getId());
            award.setTeamId(null);
            award.setTeamName(null);
            award.setTeacherSubjectId(null);
        }
        validateSubject(award);
    }

    private void validateSubject(Award award) {
        if (award.getSubjectType() == AwardSubjectType.STUDENT && award.getPrimaryStudentId() == null) {
            throw ApiException.badRequest("个人赛获奖必须选择学生");
        }
        if (award.getSubjectType() == AwardSubjectType.TEAM && award.getTeamId() == null) {
            throw ApiException.badRequest("团队赛获奖必须选择已建立团队");
        }
        if (award.getSubjectType() == AwardSubjectType.TEACHER && award.getTeacherSubjectId() == null) {
            throw ApiException.badRequest("教师获奖必须选择教师");
        }
    }

    private void normalizeSubjectFields(Award award) {
        if (award.getSubjectType() == AwardSubjectType.STUDENT) {
            award.setTeamId(null);
            award.setTeamName(null);
            award.setTeacherSubjectId(null);
            award.setTeacherAwardName(null);
        }
        if (award.getSubjectType() == AwardSubjectType.TEAM) {
            award.setPrimaryStudentId(null);
            award.setTeacherSubjectId(null);
            award.setTeacherAwardName(null);
        }
        if (award.getSubjectType() == AwardSubjectType.TEACHER) {
            award.setPrimaryStudentId(null);
            award.setTeamId(null);
            award.setTeamName(null);
        }
    }

    private void replaceAdvisors(UUID awardId, List<UUID> teacherIds) {
        advisorRepository.deleteByAwardId(awardId);
        if (teacherIds == null) {
            return;
        }
        teacherIds.stream().distinct().forEach(teacherId -> {
            teacherRepository.findById(teacherId).orElseThrow(() -> ApiException.notFound("指导老师不存在"));
            AwardAdvisor advisor = new AwardAdvisor();
            advisor.setAwardId(awardId);
            advisor.setTeacherId(teacherId);
            advisorRepository.save(advisor);
        });
    }

    private String resolveTeamName(UUID teamId) {
        if (teamId == null) {
            return null;
        }
        return teamRepository.findById(teamId).map(Team::getName).orElseThrow(() -> ApiException.notFound("团队不存在"));
    }

    private void ensureStudentInTeam(UUID studentId, UUID teamId) {
        if (teamId == null) {
            return;
        }
        boolean exists = teamMemberRepository.findByTeamId(teamId).stream()
                .anyMatch(member -> studentId.equals(member.getStudentId()));
        if (!exists) {
            throw ApiException.forbidden("只能申报自己所在团队的获奖");
        }
    }

    private void ensureStudentCanManageAward(StudentProfile student, Award award) {
        if (!canStudentManageAward(student, award)) {
            throw ApiException.forbidden("只能维护本人或所在团队的获奖记录");
        }
    }

    private boolean canStudentManageAward(StudentProfile student, Award award) {
        if (award.getDeclaredByUserId() != null) {
            UUID declaredStudentId = studentRepository.findByUserId(award.getDeclaredByUserId())
                    .map(StudentProfile::getId)
                    .orElse(null);
            if (student.getId().equals(declaredStudentId)) {
                return true;
            }
        }
        if (student.getId().equals(award.getPrimaryStudentId())) {
            return true;
        }
        return award.getTeamId() != null && teamMemberRepository.findByStudentId(student.getId()).stream()
                .anyMatch(member -> award.getTeamId().equals(member.getTeamId()));
    }

    private void ensureTeacherCanReviewAward(UUID teacherId, UUID awardId) {
        boolean advisor = advisorRepository.findByAwardId(awardId).stream()
                .anyMatch(item -> teacherId.equals(item.getTeacherId()));
        if (!advisor) {
            throw ApiException.forbidden("只有该获奖记录的指导老师可以审核");
        }
    }

    private Integer yearOf(java.time.LocalDate awardDate) {
        return awardDate == null ? null : awardDate.getYear();
    }

    private void writeReviewLog(UUID awardId, String action, String opinion, UUID reviewerId) {
        ReviewLog log = new ReviewLog();
        log.setTargetType("AWARD");
        log.setTargetId(awardId);
        log.setAction(action);
        log.setOpinion(opinion);
        log.setReviewerUserId(reviewerId);
        reviewLogRepository.save(log);
    }
}
