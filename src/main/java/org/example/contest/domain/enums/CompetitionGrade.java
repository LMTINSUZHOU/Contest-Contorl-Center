package org.example.contest.domain.enums;

/**
 * 竞赛等级枚举。数据库保存枚举名，展示层使用中文 label。
 */
public enum CompetitionGrade {
    FIRST_A("一类A"),
    FIRST_B("一类B"),
    SECOND_A("二类A"),
    SECOND_B("二类B"),
    THIRD("三类"),
    OTHER("其他");

    private final String label;

    CompetitionGrade(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
