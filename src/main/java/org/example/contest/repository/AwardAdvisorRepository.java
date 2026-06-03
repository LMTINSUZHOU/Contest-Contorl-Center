package org.example.contest.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.example.contest.domain.AwardAdvisor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AwardAdvisorRepository extends JpaRepository<AwardAdvisor, UUID> {
    List<AwardAdvisor> findByAwardId(UUID awardId);

    List<AwardAdvisor> findByAwardIdIn(Collection<UUID> awardIds);

    List<AwardAdvisor> findByTeacherId(UUID teacherId);

    void deleteByAwardId(UUID awardId);
}
