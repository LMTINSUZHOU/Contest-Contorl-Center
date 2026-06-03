package org.example.contest.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.example.contest.common.ApiException;
import org.example.contest.domain.ReviewLog;
import org.example.contest.domain.StudentProfile;
import org.example.contest.domain.TeacherProfile;
import org.example.contest.domain.UserAccount;
import org.example.contest.domain.enums.AccountStatus;
import org.example.contest.domain.enums.UserRole;
import org.example.contest.repository.ReviewLogRepository;
import org.example.contest.repository.StudentProfileRepository;
import org.example.contest.repository.TeacherProfileRepository;
import org.example.contest.repository.UserRepository;
import org.example.contest.security.JwtService;
import org.example.contest.web.dto.AuthDtos;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AccountService(
            UserRepository userRepository,
            StudentProfileRepository studentProfileRepository,
            TeacherProfileRepository teacherProfileRepository,
            ReviewLogRepository reviewLogRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.teacherProfileRepository = teacherProfileRepository;
        this.reviewLogRepository = reviewLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.UserView registerStudent(AuthDtos.RegisterStudentRequest request) {
        ensureUniqueEmail(request.email());
        if (studentProfileRepository.existsByStudentNo(request.studentNo())) {
            throw ApiException.badRequest("学号已存在");
        }
        UserAccount user = createPendingUser(request.email(), request.password(), UserRole.STUDENT);
        StudentProfile profile = new StudentProfile();
        profile.setUserId(user.getId());
        profile.setStudentNo(request.studentNo());
        profile.setName(request.name());
        profile.setGender(request.gender());
        profile.setCollege(request.college());
        profile.setMajor(request.major());
        profile.setClassName(request.className());
        profile.setGrade(request.grade());
        profile.setPhone(request.phone());
        studentProfileRepository.save(profile);
        return toUserView(user);
    }

    @Transactional
    public AuthDtos.UserView registerTeacher(AuthDtos.RegisterTeacherRequest request) {
        ensureUniqueEmail(request.email());
        if (teacherProfileRepository.existsByTeacherNo(request.teacherNo())) {
            throw ApiException.badRequest("工号已存在");
        }
        UserAccount user = createPendingUser(request.email(), request.password(), UserRole.TEACHER);
        TeacherProfile profile = new TeacherProfile();
        profile.setUserId(user.getId());
        profile.setTeacherNo(request.teacherNo());
        profile.setName(request.name());
        profile.setGender(request.gender());
        profile.setCollege(request.college());
        profile.setTitle(request.title());
        profile.setPhone(request.phone());
        teacherProfileRepository.save(profile);
        return toUserView(user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        UserAccount user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "邮箱或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "邮箱或密码错误");
        }
        if (user.getStatus() != AccountStatus.NORMAL) {
            throw new ApiException(HttpStatus.FORBIDDEN, "账号当前状态为" + user.getStatus().getLabel() + "，暂不能登录");
        }
        return new AuthDtos.AuthResponse(jwtService.issue(user), toUserView(user));
    }

    public AuthDtos.UserView me(UUID userId) {
        return toUserView(userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("用户不存在")));
    }

    public List<AuthDtos.UserView> pendingUsers() {
        return userRepository.findByStatus(AccountStatus.PENDING).stream().map(this::toUserView).toList();
    }

    @Transactional
    public AuthDtos.UserView approve(UUID userId, UUID reviewerId, String reason) {
        UserAccount user = loadUser(userId);
        user.setStatus(AccountStatus.NORMAL);
        user.setReviewReason(reason);
        writeReviewLog("USER", userId, "APPROVE", reason, reviewerId);
        return toUserView(user);
    }

    @Transactional
    public AuthDtos.UserView reject(UUID userId, UUID reviewerId, String reason) {
        UserAccount user = loadUser(userId);
        user.setStatus(AccountStatus.REJECTED);
        user.setReviewReason(reason);
        writeReviewLog("USER", userId, "REJECT", reason, reviewerId);
        return toUserView(user);
    }

    @Transactional
    public AuthDtos.UserView disable(UUID userId, UUID reviewerId, String reason) {
        UserAccount user = loadUser(userId);
        user.setStatus(AccountStatus.DISABLED);
        user.setReviewReason(reason);
        writeReviewLog("USER", userId, "DISABLE", reason, reviewerId);
        return toUserView(user);
    }

    public AuthDtos.UserView toUserView(UserAccount user) {
        String displayName = null;
        String profileNo = null;
        if (user.getRole() == UserRole.STUDENT) {
            StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
            if (profile != null) {
                displayName = profile.getName();
                profileNo = profile.getStudentNo();
            }
        } else if (user.getRole() == UserRole.TEACHER) {
            TeacherProfile profile = teacherProfileRepository.findByUserId(user.getId()).orElse(null);
            if (profile != null) {
                displayName = profile.getName();
                profileNo = profile.getTeacherNo();
            }
        } else {
            displayName = "系统管理员";
        }
        return new AuthDtos.UserView(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getRole().getLabel(),
                user.getStatus(),
                user.getStatus().getLabel(),
                displayName,
                profileNo
        );
    }

    private UserAccount createPendingUser(String email, String rawPassword, UserRole role) {
        UserAccount user = new UserAccount();
        user.setEmail(normalizeEmail(email));
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setStatus(AccountStatus.PENDING);
        return userRepository.save(user);
    }

    private void ensureUniqueEmail(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.badRequest("邮箱已注册");
        }
    }

    private UserAccount loadUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("用户不存在"));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void writeReviewLog(String targetType, UUID targetId, String action, String opinion, UUID reviewerId) {
        ReviewLog log = new ReviewLog();
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setAction(action);
        log.setOpinion(opinion);
        log.setReviewerUserId(reviewerId);
        reviewLogRepository.save(log);
    }
}
