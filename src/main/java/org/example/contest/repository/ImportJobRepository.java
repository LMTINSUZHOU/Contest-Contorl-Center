package org.example.contest.repository;

import java.util.UUID;
import org.example.contest.domain.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportJobRepository extends JpaRepository<ImportJob, UUID> {
}
