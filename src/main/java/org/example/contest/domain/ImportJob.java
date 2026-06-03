package org.example.contest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import org.example.contest.domain.enums.ImportStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "import_jobs")
public class ImportJob extends AuditableEntity {
    @Column(nullable = false, length = 40)
    private String importType;

    @Column(length = 260)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImportStatus status = ImportStatus.SUCCESS;

    @Column(nullable = false)
    private int successRows;

    @Column(nullable = false)
    private int errorRows;

    private UUID operatorUserId;
}
