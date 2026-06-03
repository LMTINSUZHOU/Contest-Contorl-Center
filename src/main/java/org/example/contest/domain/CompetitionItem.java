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
@Table(name = "competition_items")
public class CompetitionItem extends AuditableEntity {
    @Column(nullable = false)
    private UUID competitionId;

    private UUID levelId;
    private UUID trackId;

    @Column(nullable = false, length = 180)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private CompetitionGrade grade;

    @Column(nullable = false)
    private boolean enabled = true;
}
