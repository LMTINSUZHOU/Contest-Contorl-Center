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
@Table(name = "award_advisors")
public class AwardAdvisor extends AuditableEntity {
    @Column(nullable = false)
    private UUID awardId;

    @Column(nullable = false)
    private UUID teacherId;
}
