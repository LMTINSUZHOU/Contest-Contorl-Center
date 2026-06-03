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
@Table(name = "team_members")
public class TeamMember extends AuditableEntity {
    @Column(nullable = false)
    private UUID teamId;

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private boolean captain;
}
