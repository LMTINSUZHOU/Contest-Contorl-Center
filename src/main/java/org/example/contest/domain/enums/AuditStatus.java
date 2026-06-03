package org.example.contest.domain.enums;

public enum AuditStatus {
    PENDING("待审核"),
    APPROVED("审核通过"),
    REJECTED("审核驳回");

    private final String label;

    AuditStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
