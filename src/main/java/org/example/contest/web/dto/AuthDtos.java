package org.example.contest.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.example.contest.domain.enums.AccountStatus;
import org.example.contest.domain.enums.UserRole;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record LoginRequest(
            @Email(message = "邮箱格式不正确") @NotBlank(message = "邮箱不能为空") String email,
            @NotBlank(message = "密码不能为空") String password
    ) {
    }

    public record RegisterStudentRequest(
            @Email(message = "邮箱格式不正确") @NotBlank(message = "邮箱不能为空") String email,
            @NotBlank(message = "密码不能为空") String password,
            @NotBlank(message = "学号不能为空") String studentNo,
            @NotBlank(message = "姓名不能为空") String name,
            String gender,
            String college,
            String major,
            String className,
            String grade,
            String phone
    ) {
    }

    public record RegisterTeacherRequest(
            @Email(message = "邮箱格式不正确") @NotBlank(message = "邮箱不能为空") String email,
            @NotBlank(message = "密码不能为空") String password,
            @NotBlank(message = "工号不能为空") String teacherNo,
            @NotBlank(message = "姓名不能为空") String name,
            String gender,
            String college,
            String title,
            String phone
    ) {
    }

    public record UserView(
            UUID id,
            String email,
            UserRole role,
            String roleLabel,
            AccountStatus status,
            String statusLabel,
            String displayName,
            String profileNo
    ) {
    }

    public record AuthResponse(String token, UserView user) {
    }

    public record AuditRequest(String reason) {
    }
}
