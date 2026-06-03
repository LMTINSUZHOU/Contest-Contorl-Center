package org.example.contest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "review_logs")
public class ReviewLog extends AuditableEntity {
    @Column(nullable = false, length = 40)
    private String targetType;

    @Column(nullable = false)
    private UUID targetId;

    @Column(nullable = false, length = 40)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String opinion;

    private UUID reviewerUserId;
}
