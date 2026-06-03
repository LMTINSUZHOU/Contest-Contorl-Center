package org.example.contest.repository;

import java.util.Optional;
import java.util.UUID;
import org.example.contest.domain.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, UUID> {
    Optional<TeacherProfile> findByUserId(UUID userId);

    Optional<TeacherProfile> findByTeacherNo(String teacherNo);

    boolean existsByTeacherNo(String teacherNo);
}
