package org.example.contest.repository;

import java.util.Optional;
import java.util.UUID;
import org.example.contest.domain.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {
    Optional<StudentProfile> findByUserId(UUID userId);

    Optional<StudentProfile> findByStudentNo(String studentNo);

    boolean existsByStudentNo(String studentNo);
}
