package org.example.contest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.example.contest.domain.enums.AccountStatus;
import org.example.contest.domain.enums.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 登录账号表。学生、教师的业务资料分别存放在 profile 表中。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserAccount extends AuditableEntity {
    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(columnDefinition = "TEXT")
    private String reviewReason;
}
