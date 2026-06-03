package org.example.contest.domain.enums;

public enum AwardLevel {
    GRAND_PRIZE("特等奖"),
    FIRST_PRIZE("一等奖"),
    SECOND_PRIZE("二等奖"),
    THIRD_PRIZE("三等奖"),
    EXCELLENCE("优秀奖"),
    EXCELLENT_ADVISOR("优秀指导老师奖");

    private final String label;

    AwardLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
