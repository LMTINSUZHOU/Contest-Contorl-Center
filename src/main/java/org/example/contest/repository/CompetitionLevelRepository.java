package org.example.contest.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.contest.domain.CompetitionLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionLevelRepository extends JpaRepository<CompetitionLevel, UUID> {
    List<CompetitionLevel> findByCompetitionIdOrderBySortOrderAscNameAsc(UUID competitionId);

    Optional<CompetitionLevel> findByCompetitionIdAndNameIgnoreCase(UUID competitionId, String name);
}
