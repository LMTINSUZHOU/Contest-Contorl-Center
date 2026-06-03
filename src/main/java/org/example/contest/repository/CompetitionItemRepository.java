package org.example.contest.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.contest.domain.CompetitionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionItemRepository extends JpaRepository<CompetitionItem, UUID> {
    List<CompetitionItem> findByCompetitionIdOrderByNameAsc(UUID competitionId);

    List<CompetitionItem> findByCompetitionIdAndTrackIdOrderByNameAsc(UUID competitionId, UUID trackId);

    Optional<CompetitionItem> findByCompetitionIdAndNameIgnoreCase(UUID competitionId, String name);

    Optional<CompetitionItem> findByCompetitionIdAndLevelIdAndTrackIdAndNameIgnoreCase(
            UUID competitionId,
            UUID levelId,
            UUID trackId,
            String name
    );
}
