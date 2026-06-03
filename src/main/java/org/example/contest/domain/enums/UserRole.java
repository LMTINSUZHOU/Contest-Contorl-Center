package org.example.contest.domain.enums;

public enum UserRole {
    ADMIN("管理员"),
    STUDENT("学生"),
    TEACHER("指导老师");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
