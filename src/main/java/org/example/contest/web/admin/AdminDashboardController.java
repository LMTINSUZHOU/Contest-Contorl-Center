package org.example.contest.web.admin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.example.contest.domain.Award;
import org.example.contest.domain.enums.AccountStatus;
import org.example.contest.domain.enums.AuditStatus;
import org.example.contest.domain.enums.AwardSubjectType;
import org.example.contest.domain.enums.CompetitionGrade;
import org.example.contest.repository.AwardRepository;
import org.example.contest.repository.CertificateFileRepository;
import org.example.contest.repository.CompetitionRepository;
import org.example.contest.repository.StudentProfileRepository;
import org.example.contest.repository.TeacherProfileRepository;
import org.example.contest.repository.UserRepository;
import org.example.contest.web.dto.ManagementDtos;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {
    private final StudentProfileRepository studentRepository;
    private final TeacherProfileRepository teacherRepository;
    private final CompetitionRepository competitionRepository;
    private final AwardRepository awardRepository;
    private final UserRepository userRepository;
    private final CertificateFileRepository certificateRepository;

    public AdminDashboardController(
            StudentProfileRepository studentRepository,
            TeacherProfileRepository teacherRepository,
            CompetitionRepository competitionRepository,
            AwardRepository awardRepository,
            UserRepository userRepository,
            CertificateFileRepository certificateRepository
    ) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.competitionRepository = competitionRepository;
        this.awardRepository = awardRepository;
        this.userRepository = userRepository;
        this.certificateRepository = certificateRepository;
    }

    @GetMapping("/dashboard/summary")
    public ManagementDtos.DashboardSummary summary() {
        return new ManagementDtos.DashboardSummary(
                studentRepository.count(),
                teacherRepository.count(),
                competitionRepository.count(),
                awardRepository.count(),
                userRepository.countByStatus(AccountStatus.PENDING),
                awardRepository.countByAuditStatus(AuditStatus.PENDING),
                certificateRepository.countByActiveTrue(),
                awardRepository.countByCompetitionGrade(CompetitionGrade.FIRST_A)
                        + awardRepository.countByCompetitionGrade(CompetitionGrade.FIRST_B),
                awardRepository.countByCompetitionGrade(CompetitionGrade.SECOND_A)
                        + awardRepository.countByCompetitionGrade(CompetitionGrade.SECOND_B),
                awardRepository.countByCompetitionGrade(CompetitionGrade.FIRST_A),
                awardRepository.countByCompetitionGrade(CompetitionGrade.FIRST_B),
                awardRepository.countByCompetitionGrade(CompetitionGrade.SECOND_A),
                awardRepository.countByCompetitionGrade(CompetitionGrade.SECOND_B),
                awardRepository.countBySubjectType(AwardSubjectType.STUDENT),
                awardRepository.countBySubjectType(AwardSubjectType.TEAM),
                awardRepository.countBySubjectType(AwardSubjectType.TEACHER)
        );
    }

    @GetMapping("/statistics/awards")
    public Map<String, Long> awardStatistics(@RequestParam(defaultValue = "year") String dimension) {
        List<Award> awards = awardRepository.findAll();
        return switch (dimension) {
            case "competitionGrade" -> awards.stream().collect(Collectors.groupingBy(
                    award -> award.getCompetitionGrade().getLabel(),
                    Collectors.counting()
            ));
            case "awardLevel" -> awards.stream().collect(Collectors.groupingBy(
                    award -> award.getAwardLevel().getLabel(),
                    Collectors.counting()
            ));
            case "subjectType" -> awards.stream().collect(Collectors.groupingBy(
                    award -> award.getSubjectType().getLabel(),
                    Collectors.counting()
            ));
            default -> awards.stream().collect(Collectors.groupingBy(
                    award -> award.getAwardYear() == null ? "未填写年份" : String.valueOf(award.getAwardYear()),
                    Collectors.counting()
            ));
        };
    }
}
