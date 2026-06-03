package org.example.contest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.example.contest.domain.enums.CompetitionGrade;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "competitions")
public class Competition extends AuditableEntity {
    @Column(nullable = false, unique = true, length = 180)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CompetitionGrade defaultGrade = CompetitionGrade.OTHER;

    @Column(length = 240)
    private String organizer;

    @Column(length = 240)
    private String coOrganizer;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String websiteUrl;

    @Column(nullable = false)
    private boolean enabled = true;
}
