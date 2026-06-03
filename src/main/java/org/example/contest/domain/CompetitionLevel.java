package org.example.contest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import org.example.contest.domain.enums.CompetitionGrade;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "competition_levels")
public class CompetitionLevel extends AuditableEntity {
    @Column(nullable = false)
    private UUID competitionId;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private CompetitionGrade grade;

    @Column(nullable = false)
    private int sortOrder = 0;

    @Column(nullable = false)
    private boolean enabled = true;
}
