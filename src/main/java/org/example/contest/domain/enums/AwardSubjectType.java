package org.example.contest.domain.enums;

public enum AwardSubjectType {
    STUDENT("个人赛"),
    TEAM("团队赛"),
    TEACHER("教师获奖");

    private final String label;

    AwardSubjectType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
