package org.example.contest.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.contest.domain.UserAccount;
import org.example.contest.domain.enums.AccountStatus;
import org.example.contest.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<UserAccount> findByStatus(AccountStatus status);

    long countByStatus(AccountStatus status);

    long countByRole(UserRole role);
}
