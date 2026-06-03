package org.example.contest.repository;

import java.util.UUID;
import org.example.contest.domain.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, UUID> {
}
