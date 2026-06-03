package org.example.contest.repository;

import java.util.List;
import java.util.UUID;
import org.example.contest.domain.ImportErrorRow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportErrorRowRepository extends JpaRepository<ImportErrorRow, UUID> {
    List<ImportErrorRow> findByJobIdOrderByRowIndexAsc(UUID jobId);
}
