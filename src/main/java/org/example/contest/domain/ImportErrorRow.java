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
@Table(name = "import_error_rows")
public class ImportErrorRow extends AuditableEntity {
    @Column(nullable = false)
    private UUID jobId;

    @Column(nullable = false)
    private int rowIndex;

    @Column(nullable = false, length = 120)
    private String fieldName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String rawValue;
}
