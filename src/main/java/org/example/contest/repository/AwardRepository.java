package org.example.contest.repository;

import java.util.List;
import java.util.UUID;
import org.example.contest.domain.Award;
import org.example.contest.domain.enums.AuditStatus;
import org.example.contest.domain.enums.AwardSubjectType;
import org.example.contest.domain.enums.CompetitionGrade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AwardRepository extends JpaRepository<Award, UUID> {
    List<Award> findByAuditStatus(AuditStatus status);

    long countByAuditStatus(AuditStatus status);

    long countByCompetitionGrade(CompetitionGrade competitionGrade);

    long countBySubjectType(AwardSubjectType subjectType);
}
