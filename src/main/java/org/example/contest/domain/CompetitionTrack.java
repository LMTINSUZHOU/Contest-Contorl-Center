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
@Table(name = "competition_tracks")
public class CompetitionTrack extends AuditableEntity {
    @Column(nullable = false)
    private UUID competitionId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private boolean enabled = true;
}
