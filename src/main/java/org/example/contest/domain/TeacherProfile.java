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
@Table(name = "teacher_profiles")
public class TeacherProfile extends AuditableEntity {
    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 60)
    private String teacherNo;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 20)
    private String gender;

    @Column(length = 120)
    private String college;

    @Column(length = 120)
    private String title;

    @Column(length = 40)
    private String phone;
}
