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
@Table(name = "teams")
public class Team extends AuditableEntity {
    @Column(nullable = false, length = 160)
    private String name;

    private UUID captainStudentId;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
