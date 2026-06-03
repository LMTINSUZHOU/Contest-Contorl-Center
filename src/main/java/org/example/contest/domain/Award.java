package org.example.contest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import org.example.contest.domain.enums.AuditStatus;
import org.example.contest.domain.enums.AwardLevel;
import org.example.contest.domain.enums.AwardSubjectType;
import org.example.contest.domain.enums.CompetitionGrade;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 获奖主表。个人赛、团队赛、教师获奖共用一张表，通过 subjectType 区分获奖主体。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "awards")
public class Award extends AuditableEntity {
    @Column(nullable = false)
    private UUID competitionId;

    @Column(length = 180)
    private String competitionAlias;

    private UUID levelId;
    private UUID trackId;
    private UUID itemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CompetitionGrade competitionGrade = CompetitionGrade.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private AwardLevel awardLevel = AwardLevel.EXCELLENCE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AwardSubjectType subjectType = AwardSubjectType.STUDENT;

    private Integer awardYear;
    private LocalDate awardDate;

    @Column(length = 160)
    private String awardLocation;

    private UUID primaryStudentId;
    private UUID teacherSubjectId;
    private UUID teamId;

    @Column(length = 160)
    private String teamName;

    @Column(nullable = false)
    private boolean teamAward;

    @Column(length = 160)
    private String teacherAwardName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditStatus auditStatus = AuditStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String auditOpinion;

    private UUID declaredByUserId;
    private UUID createdByUserId;
}
