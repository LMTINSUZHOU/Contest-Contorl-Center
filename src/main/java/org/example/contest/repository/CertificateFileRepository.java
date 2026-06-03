package org.example.contest.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.contest.domain.CertificateFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateFileRepository extends JpaRepository<CertificateFile, UUID> {
    List<CertificateFile> findByAwardId(UUID awardId);

    Optional<CertificateFile> findFirstByAwardIdAndActiveTrueOrderByCreatedAtDesc(UUID awardId);

    long countByActiveTrue();
}
