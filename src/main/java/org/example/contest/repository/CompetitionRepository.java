package org.example.contest.repository;

import java.util.Optional;
import java.util.UUID;
import org.example.contest.domain.Competition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionRepository extends JpaRepository<Competition, UUID> {
    Optional<Competition> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
