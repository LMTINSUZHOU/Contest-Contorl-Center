package org.example.contest.repository;

import java.util.List;
import java.util.UUID;
import org.example.contest.domain.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    List<TeamMember> findByTeamId(UUID teamId);

    List<TeamMember> findByStudentId(UUID studentId);

    void deleteByTeamId(UUID teamId);
}
