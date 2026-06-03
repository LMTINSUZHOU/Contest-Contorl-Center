package org.example.contest.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.contest.domain.CompetitionTrack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionTrackRepository extends JpaRepository<CompetitionTrack, UUID> {
    List<CompetitionTrack> findByCompetitionIdOrderByNameAsc(UUID competitionId);

    Optional<CompetitionTrack> findByCompetitionIdAndNameIgnoreCase(UUID competitionId, String name);
}
