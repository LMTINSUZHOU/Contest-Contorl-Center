package org.example.contest.web.admin;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.example.contest.common.ApiException;
import org.example.contest.domain.StudentProfile;
import org.example.contest.domain.TeacherProfile;
import org.example.contest.domain.UserAccount;
import org.example.contest.domain.enums.AccountStatus;
import org.example.contest.domain.enums.UserRole;
import org.example.contest.repository.StudentProfileRepository;
import org.example.contest.repository.TeacherProfileRepository;
import org.example.contest.repository.UserRepository;
import org.example.contest.web.dto.ManagementDtos;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminProfileController {
    private static final String DEFAULT_STUDENT_PASSWORD = "Student@123456";
    private static final String DEFAULT_TEACHER_PASSWORD = "Teacher@123456";

    private final StudentProfileRepository studentRepository;
    private final TeacherProfileRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminProfileController(
            StudentProfileRepository studentRepository,
            TeacherProfileRepository teacherRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/students")
    public List<StudentProfile> students(@RequestParam(required = false) String q) {
        return studentRepository.findAll().stream()
                .filter(student -> contains(student.getName(), q) || contains(student.getStudentNo(), q)
                        || contains(student.getCollege(), q) || contains(student.getMajor(), q))
                .toList();
    }

    @GetMapping("/students/{id}")
    public StudentProfile student(@PathVariable UUID id) {
        return studentRepository.findById(id).orElseThrow(() -> ApiException.notFound("学生不存在"));
    }

    @Transactional
    @PostMapping("/students")
    public StudentProfile createStudent(@Valid @RequestBody ManagementDtos.StudentRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw ApiException.badRequest("管理员新增学生时必须填写邮箱");
        }
        if (studentRepository.existsByStudentNo(request.studentNo())) {
            throw ApiException.badRequest("学号已存在");
        }
        UserAccount user = createUser(request.email(), request.password(), UserRole.STUDENT, DEFAULT_STUDENT_PASSWORD);
        StudentProfile student = new StudentProfile();
        applyStudent(student, request);
        student.setUserId(user.getId());
        return studentRepository.save(student);
    }

    @Transactional
    @PutMapping("/students/{id}")
    public StudentProfile updateStudent(@PathVariable UUID id, @Valid @RequestBody ManagementDtos.StudentRequest request) {
        StudentProfile student = student(id);
        studentRepository.findByStudentNo(request.studentNo())
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw ApiException.badRequest("学号已存在");
                });
        applyStudent(student, request);
        updateUser(student.getUserId(), request.email(), request.password());
        return student;
    }

    @Transactional
    @PostMapping("/students/{id}/password")
    public void resetStudentPassword(@PathVariable UUID id, @Valid @RequestBody ManagementDtos.PasswordResetRequest request) {
        resetPassword(student(id).getUserId(), request.password());
    }

    @Transactional
    @DeleteMapping("/students/{id}")
    public void deleteStudent(@PathVariable UUID id) {
        StudentProfile student = student(id);
        userRepository.deleteById(student.getUserId());
    }

    @GetMapping("/teachers")
    public List<TeacherProfile> teachers(@RequestParam(required = false) String q) {
        return teacherRepository.findAll().stream()
                .filter(teacher -> contains(teacher.getName(), q) || contains(teacher.getTeacherNo(), q)
                        || contains(teacher.getCollege(), q) || contains(teacher.getTitle(), q))
                .toList();
    }

    @GetMapping("/teachers/{id}")
    public TeacherProfile teacher(@PathVariable UUID id) {
        return teacherRepository.findById(id).orElseThrow(() -> ApiException.notFound("教师不存在"));
    }

    @Transactional
    @PostMapping("/teachers")
    public TeacherProfile createTeacher(@Valid @RequestBody ManagementDtos.TeacherRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw ApiException.badRequest("管理员新增教师时必须填写邮箱");
        }
        if (teacherRepository.existsByTeacherNo(request.teacherNo())) {
            throw ApiException.badRequest("工号已存在");
        }
        UserAccount user = createUser(request.email(), request.password(), UserRole.TEACHER, DEFAULT_TEACHER_PASSWORD);
        TeacherProfile teacher = new TeacherProfile();
        applyTeacher(teacher, request);
        teacher.setUserId(user.getId());
        return teacherRepository.save(teacher);
    }

    @Transactional
    @PutMapping("/teachers/{id}")
    public TeacherProfile updateTeacher(@PathVariable UUID id, @Valid @RequestBody ManagementDtos.TeacherRequest request) {
        TeacherProfile teacher = teacher(id);
        teacherRepository.findByTeacherNo(request.teacherNo())
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw ApiException.badRequest("工号已存在");
                });
        applyTeacher(teacher, request);
        updateUser(teacher.getUserId(), request.email(), request.password());
        return teacher;
    }

    @Transactional
    @PostMapping("/teachers/{id}/password")
    public void resetTeacherPassword(@PathVariable UUID id, @Valid @RequestBody ManagementDtos.PasswordResetRequest request) {
        resetPassword(teacher(id).getUserId(), request.password());
    }

    @Transactional
    @DeleteMapping("/teachers/{id}")
    public void deleteTeacher(@PathVariable UUID id) {
        TeacherProfile teacher = teacher(id);
        userRepository.deleteById(teacher.getUserId());
    }

    private UserAccount createUser(String email, String password, UserRole role, String defaultPassword) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.badRequest("邮箱已存在");
        }
        UserAccount user = new UserAccount();
        user.setEmail(normalizeEmail(email));
        user.setPasswordHash(passwordEncoder.encode(password == null || password.isBlank() ? defaultPassword : password));
        user.setRole(role);
        user.setStatus(AccountStatus.NORMAL);
        return userRepository.save(user);
    }

    private void updateUser(UUID userId, String email, String password) {
        UserAccount user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("账号不存在"));
        if (email != null && !email.isBlank() && !user.getEmail().equalsIgnoreCase(email)) {
            if (userRepository.existsByEmailIgnoreCase(email)) {
                throw ApiException.badRequest("邮箱已存在");
            }
            user.setEmail(normalizeEmail(email));
        }
        if (password != null && !password.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }
    }

    private void resetPassword(UUID userId, String password) {
        UserAccount user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("账号不存在"));
        user.setPasswordHash(passwordEncoder.encode(password));
    }

    private void applyStudent(StudentProfile student, ManagementDtos.StudentRequest request) {
        student.setStudentNo(request.studentNo());
        student.setName(request.name());
        student.setGender(request.gender());
        student.setCollege(request.college());
        student.setMajor(request.major());
        student.setClassName(request.className());
        student.setGrade(request.grade());
        student.setPhone(request.phone());
    }

    private void applyTeacher(TeacherProfile teacher, ManagementDtos.TeacherRequest request) {
        teacher.setTeacherNo(request.teacherNo());
        teacher.setName(request.name());
        teacher.setGender(request.gender());
        teacher.setCollege(request.college());
        teacher.setTitle(request.title());
        teacher.setPhone(request.phone());
    }

    private boolean contains(String value, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        return value != null && value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
