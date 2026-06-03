package org.example.contest.domain.enums;

/**
 * 用户账号生命周期。只有 NORMAL 能登录，其余状态均由安全层拒绝。
 */
public enum AccountStatus {
    PENDING("待审核"),
    NORMAL("正常"),
    REJECTED("驳回"),
    DISABLED("禁用");

    private final String label;

    AccountStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
