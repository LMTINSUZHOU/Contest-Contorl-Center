package org.example.contest.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.example.contest.domain.enums.AuditStatus;
import org.example.contest.domain.enums.AwardLevel;
import org.example.contest.domain.enums.AwardSubjectType;
import org.example.contest.domain.enums.CompetitionGrade;

public final class ManagementDtos {
    private ManagementDtos() {
    }

    public record StudentRequest(
            @NotBlank(message = "学号不能为空") String studentNo,
            @NotBlank(message = "姓名不能为空") String name,
            String gender,
            String college,
            String major,
            String className,
            String grade,
            String phone,
            String email,
            String password
    ) {
    }

    public record TeacherRequest(
            @NotBlank(message = "工号不能为空") String teacherNo,
            @NotBlank(message = "姓名不能为空") String name,
            String gender,
            String college,
            String title,
            String phone,
            String email,
            String password
    ) {
    }

    public record PasswordResetRequest(
            @NotBlank(message = "新密码不能为空") String password
    ) {
    }

    public record CompetitionRequest(
            @NotBlank(message = "竞赛名称不能为空") String name,
            @NotNull(message = "竞赛等级不能为空") CompetitionGrade defaultGrade,
            String organizer,
            String coOrganizer,
            String description,
            String websiteUrl,
            Boolean enabled
    ) {
    }

    public record CompetitionTrackRequest(
            @NotNull(message = "竞赛不能为空") UUID competitionId,
            @NotBlank(message = "赛道名称不能为空") String name,
            Boolean enabled
    ) {
    }

    public record TeamRequest(
            @NotBlank(message = "团队名称不能为空") String name,
            UUID captainStudentId,
            List<UUID> memberStudentIds,
            String notes
    ) {
    }

    public record AwardRequest(
            @NotNull(message = "竞赛不能为空") UUID competitionId,
            @NotNull(message = "赛道不能为空") UUID trackId,
            String competitionAlias,
            @NotNull(message = "获奖等级不能为空") AwardLevel awardLevel,
            @NotNull(message = "获奖主体不能为空") AwardSubjectType subjectType,
            @NotNull(message = "获奖日期不能为空") LocalDate awardDate,
            @NotBlank(message = "获奖地点不能为空") String awardLocation,
            UUID primaryStudentId,
            UUID teacherSubjectId,
            UUID teamId,
            String teacherAwardName,
            List<UUID> advisorTeacherIds,
            AuditStatus auditStatus,
            String auditOpinion
    ) {
    }

    public record AwardDeclarationRequest(
            @NotNull(message = "竞赛不能为空") UUID competitionId,
            @NotNull(message = "赛道不能为空") UUID trackId,
            String competitionAlias,
            UUID teamId,
            Boolean teamAward,
            @NotNull(message = "获奖等级不能为空") AwardLevel awardLevel,
            @NotNull(message = "获奖日期不能为空") LocalDate awardDate,
            @NotBlank(message = "获奖地点不能为空") String awardLocation,
            List<UUID> advisorTeacherIds
    ) {
    }

    public record AwardView(
            UUID id,
            UUID competitionId,
            String competitionName,
            String competitionAlias,
            UUID trackId,
            String trackName,
            CompetitionGrade competitionGrade,
            String competitionGradeLabel,
            AwardLevel awardLevel,
            String awardLevelLabel,
            AwardSubjectType subjectType,
            String subjectTypeLabel,
            Integer awardYear,
            LocalDate awardDate,
            String awardLocation,
            UUID primaryStudentId,
            String studentName,
            UUID teacherSubjectId,
            String teacherName,
            UUID teamId,
            String teamName,
            List<UUID> advisorTeacherIds,
            List<String> advisorNames,
            AuditStatus auditStatus,
            String auditStatusLabel,
            String auditOpinion,
            UUID certificateId
    ) {
    }

    public record CertificateView(UUID id, UUID awardId, String originalName, String contentType, long fileSize) {
    }

    public record DashboardSummary(
            long studentTotal,
            long teacherTotal,
            long competitionTotal,
            long awardTotal,
            long pendingUserTotal,
            long pendingAwardTotal,
            long certificateTotal,
            long firstCategoryAwardTotal,
            long secondCategoryAwardTotal,
            long firstAAwardTotal,
            long firstBAwardTotal,
            long secondAAwardTotal,
            long secondBAwardTotal,
            long personalAwardTotal,
            long teamAwardTotal,
            long teacherAwardTotal
    ) {
    }
}
