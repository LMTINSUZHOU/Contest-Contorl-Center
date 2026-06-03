package org.example.contest.web.dto;

import java.util.UUID;
import org.example.contest.domain.enums.ImportStatus;

public final class ImportDtos {
    private ImportDtos() {
    }

    public record ImportResult(UUID jobId, ImportStatus status, int successRows, int errorRows) {
    }
}
