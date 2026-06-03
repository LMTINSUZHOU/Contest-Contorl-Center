package org.example.contest.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.contest.common.ApiException;
import org.example.contest.domain.AwardAdvisor;
import org.example.contest.domain.Competition;
import org.example.contest.domain.CompetitionTrack;
import org.example.contest.domain.ImportErrorRow;
import org.example.contest.domain.ImportJob;
import org.example.contest.domain.StudentProfile;
import org.example.contest.domain.Team;
import org.example.contest.domain.TeacherProfile;
import org.example.contest.domain.UserAccount;
import org.example.contest.domain.enums.AccountStatus;
import org.example.contest.domain.enums.AuditStatus;
import org.example.contest.domain.enums.AwardLevel;
import org.example.contest.domain.enums.AwardSubjectType;
import org.example.contest.domain.enums.CompetitionGrade;
import org.example.contest.domain.enums.ImportStatus;
import org.example.contest.domain.enums.UserRole;
import org.example.contest.repository.AwardAdvisorRepository;
import org.example.contest.repository.AwardRepository;
import org.example.contest.repository.CompetitionRepository;
import org.example.contest.repository.CompetitionTrackRepository;
import org.example.contest.repository.ImportErrorRowRepository;
import org.example.contest.repository.ImportJobRepository;
import org.example.contest.repository.StudentProfileRepository;
import org.example.contest.repository.TeamRepository;
import org.example.contest.repository.TeacherProfileRepository;
import org.example.contest.repository.UserRepository;
import org.example.contest.web.dto.ImportDtos;
import org.example.contest.web.dto.ManagementDtos;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImportExportService {
    private static final Map<String, List<String>> HEADERS = Map.of(
            "students", List.of("邮箱", "初始密码", "学号", "姓名", "性别", "学院", "专业", "班级", "年级", "手机号"),
            "teachers", List.of("邮箱", "初始密码", "工号", "姓名", "性别", "学院", "职称", "手机号"),
            "competitions", List.of("竞赛名称", "竞赛等级", "主办单位", "承办单位", "竞赛简介", "官网链接", "是否启用"),
            "awards", List.of("竞赛名称", "赛道", "竞赛别名", "获奖主体", "获奖等级", "学生学号或团队名称", "指导老师工号(逗号分隔)", "获奖日期", "获奖地点", "审核状态")
    );

    private final UserRepository userRepository;
    private final StudentProfileRepository studentRepository;
    private final TeacherProfileRepository teacherRepository;
    private final CompetitionRepository competitionRepository;
    private final CompetitionTrackRepository trackRepository;
    private final TeamRepository teamRepository;
    private final AwardRepository awardRepository;
    private final AwardAdvisorRepository advisorRepository;
    private final ImportJobRepository importJobRepository;
    private final ImportErrorRowRepository importErrorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AwardService awardService;

    public ImportExportService(
            UserRepository userRepository,
            StudentProfileRepository studentRepository,
            TeacherProfileRepository teacherRepository,
            CompetitionRepository competitionRepository,
            CompetitionTrackRepository trackRepository,
            TeamRepository teamRepository,
            AwardRepository awardRepository,
            AwardAdvisorRepository advisorRepository,
            ImportJobRepository importJobRepository,
            ImportErrorRowRepository importErrorRepository,
            PasswordEncoder passwordEncoder,
            AwardService awardService
    ) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.competitionRepository = competitionRepository;
        this.trackRepository = trackRepository;
        this.teamRepository = teamRepository;
        this.awardRepository = awardRepository;
        this.advisorRepository = advisorRepository;
        this.importJobRepository = importJobRepository;
        this.importErrorRepository = importErrorRepository;
        this.passwordEncoder = passwordEncoder;
        this.awardService = awardService;
    }

    public byte[] template(String type) {
        return workbookBytes(type + "-template", HEADERS.get(requireType(type)), List.of());
    }

    public byte[] export(String type) {
        type = requireType(type);
        return switch (type) {
            case "students" -> workbookBytes(type, HEADERS.get(type), studentRepository.findAll().stream()
                    .map(student -> List.of(
                            emailOf(student.getUserId()),
                            "",
                            student.getStudentNo(),
                            student.getName(),
                            safe(student.getGender()),
                            safe(student.getCollege()),
                            safe(student.getMajor()),
                            safe(student.getClassName()),
                            safe(student.getGrade()),
                            safe(student.getPhone())
                    )).toList());
            case "teachers" -> workbookBytes(type, HEADERS.get(type), teacherRepository.findAll().stream()
                    .map(teacher -> List.of(
                            emailOf(teacher.getUserId()),
                            "",
                            teacher.getTeacherNo(),
                            teacher.getName(),
                            safe(teacher.getGender()),
                            safe(teacher.getCollege()),
                            safe(teacher.getTitle()),
                            safe(teacher.getPhone())
                    )).toList());
            case "competitions" -> workbookBytes(type, HEADERS.get(type), competitionRepository.findAll().stream()
                    .map(competition -> List.of(
                            competition.getName(),
                            competition.getDefaultGrade().getLabel(),
                            safe(competition.getOrganizer()),
                            safe(competition.getCoOrganizer()),
                            safe(competition.getDescription()),
                            safe(competition.getWebsiteUrl()),
                            competition.isEnabled() ? "是" : "否"
                    )).toList());
            case "awards" -> workbookBytes(type, HEADERS.get(type), awardRepository.findAll().stream()
                    .map(award -> List.of(
                            safe(nameOfCompetition(award.getCompetitionId())),
                            safe(nameOfTrack(award.getTrackId())),
                            safe(award.getCompetitionAlias()),
                            award.getSubjectType().getLabel(),
                            award.getAwardLevel().getLabel(),
                            subjectValueOf(award.getSubjectType(), award.getPrimaryStudentId(), award.getTeacherSubjectId(), award.getTeamName()),
                            advisorNos(award.getId()),
                            award.getAwardDate() == null ? "" : award.getAwardDate().toString(),
                            safe(award.getAwardLocation()),
                            award.getAuditStatus().getLabel()
                    )).toList());
            default -> throw ApiException.badRequest("不支持的导出类型");
        };
    }

    @Transactional
    public ImportDtos.ImportResult importFile(String type, MultipartFile file, UUID operatorUserId) {
        type = requireType(type);
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("导入文件不能为空");
        }
        ImportJob job = new ImportJob();
        job.setImportType(type);
        job.setFileName(file.getOriginalFilename());
        job.setStatus(ImportStatus.SUCCESS);
        job.setOperatorUserId(operatorUserId);
        importJobRepository.save(job);

        int success = 0;
        int errors = 0;
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlank(read(row, 0, formatter))) {
                    continue;
                }
                try {
                    switch (type) {
                        case "students" -> importStudent(row, formatter);
                        case "teachers" -> importTeacher(row, formatter);
                        case "competitions" -> importCompetition(row, formatter);
                        case "awards" -> importAward(row, formatter, operatorUserId);
                        default -> throw ApiException.badRequest("不支持的导入类型");
                    }
                    success++;
                } catch (Exception exception) {
                    errors++;
                    saveError(job.getId(), rowIndex + 1, "整行", exception.getMessage(), "");
                }
            }
        } catch (IOException exception) {
            throw ApiException.badRequest("读取 Excel 失败：" + exception.getMessage());
        }
        job.setSuccessRows(success);
        job.setErrorRows(errors);
        job.setStatus(errors == 0 ? ImportStatus.SUCCESS : (success == 0 ? ImportStatus.FAILED : ImportStatus.PARTIAL_FAILED));
        return new ImportDtos.ImportResult(job.getId(), job.getStatus(), success, errors);
    }

    public byte[] errorReport(UUID jobId) {
        List<List<String>> rows = importErrorRepository.findByJobIdOrderByRowIndexAsc(jobId).stream()
                .map(error -> List.of(
                        String.valueOf(error.getRowIndex()),
                        error.getFieldName(),
                        error.getMessage(),
                        safe(error.getRawValue())
                ))
                .toList();
        return workbookBytes("import-errors", List.of("行号", "字段", "错误原因", "原始值"), rows);
    }

    private void importStudent(Row row, DataFormatter formatter) {
        String email = requireCell(row, 0, formatter, "邮箱");
        String studentNo = requireCell(row, 2, formatter, "学号");
        String name = requireCell(row, 3, formatter, "姓名");
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.badRequest("邮箱已存在");
        }
        if (studentRepository.existsByStudentNo(studentNo)) {
            throw ApiException.badRequest("学号已存在");
        }
        UserAccount user = createUser(email, read(row, 1, formatter), UserRole.STUDENT, "Student@123456");
        StudentProfile student = new StudentProfile();
        student.setUserId(user.getId());
        student.setStudentNo(studentNo);
        student.setName(name);
        student.setGender(read(row, 4, formatter));
        student.setCollege(read(row, 5, formatter));
        student.setMajor(read(row, 6, formatter));
        student.setClassName(read(row, 7, formatter));
        student.setGrade(read(row, 8, formatter));
        student.setPhone(read(row, 9, formatter));
        studentRepository.save(student);
    }

    private void importTeacher(Row row, DataFormatter formatter) {
        String email = requireCell(row, 0, formatter, "邮箱");
        String teacherNo = requireCell(row, 2, formatter, "工号");
        String name = requireCell(row, 3, formatter, "姓名");
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.badRequest("邮箱已存在");
        }
        if (teacherRepository.existsByTeacherNo(teacherNo)) {
            throw ApiException.badRequest("工号已存在");
        }
        UserAccount user = createUser(email, read(row, 1, formatter), UserRole.TEACHER, "Teacher@123456");
        TeacherProfile teacher = new TeacherProfile();
        teacher.setUserId(user.getId());
        teacher.setTeacherNo(teacherNo);
        teacher.setName(name);
        teacher.setGender(read(row, 4, formatter));
        teacher.setCollege(read(row, 5, formatter));
        teacher.setTitle(read(row, 6, formatter));
        teacher.setPhone(read(row, 7, formatter));
        teacherRepository.save(teacher);
    }

    private void importCompetition(Row row, DataFormatter formatter) {
        String name = requireCell(row, 0, formatter, "竞赛名称");
        if (competitionRepository.existsByNameIgnoreCase(name)) {
            throw ApiException.badRequest("竞赛名称已存在");
        }
        Competition competition = new Competition();
        competition.setName(name);
        competition.setDefaultGrade(parseCompetitionGrade(requireCell(row, 1, formatter, "竞赛等级")));
        competition.setOrganizer(read(row, 2, formatter));
        competition.setCoOrganizer(read(row, 3, formatter));
        competition.setDescription(read(row, 4, formatter));
        competition.setWebsiteUrl(read(row, 5, formatter));
        competition.setEnabled(!"否".equals(read(row, 6, formatter)));
        competitionRepository.save(competition);
    }

    private void importAward(Row row, DataFormatter formatter, UUID operatorUserId) {
        Competition competition = competitionRepository.findByNameIgnoreCase(requireCell(row, 0, formatter, "竞赛名称"))
                .orElseThrow(() -> ApiException.badRequest("竞赛不存在"));
        CompetitionTrack track = findTrack(competition.getId(), requireCell(row, 1, formatter, "赛道"));
        AwardSubjectType subjectType = parseSubjectType(requireCell(row, 3, formatter, "获奖主体"));
        String subjectValue = requireCell(row, 5, formatter, "学生学号或团队名称");
        StudentProfile student = subjectType == AwardSubjectType.STUDENT
                ? studentRepository.findByStudentNo(subjectValue)
                        .orElseThrow(() -> ApiException.badRequest("学生学号不存在：" + subjectValue))
                : null;
        Team team = subjectType == AwardSubjectType.TEAM
                ? teamRepository.findByNameIgnoreCase(subjectValue)
                        .orElseThrow(() -> ApiException.badRequest("团队不存在，请先建立团队：" + subjectValue))
                : null;
        TeacherProfile teacher = subjectType == AwardSubjectType.TEACHER
                ? teacherRepository.findByTeacherNo(subjectValue)
                        .orElseThrow(() -> ApiException.badRequest("教师工号不存在：" + subjectValue))
                : null;
        ManagementDtos.AwardRequest request = new ManagementDtos.AwardRequest(
                competition.getId(),
                track.getId(),
                read(row, 2, formatter),
                parseAwardLevel(requireCell(row, 4, formatter, "获奖等级")),
                subjectType,
                parseDate(requireCell(row, 7, formatter, "获奖日期")),
                requireCell(row, 8, formatter, "获奖地点"),
                student == null ? null : student.getId(),
                teacher == null ? null : teacher.getId(),
                team == null ? null : team.getId(),
                null,
                parseAdvisorIds(read(row, 6, formatter)),
                parseAuditStatus(read(row, 9, formatter)),
                "批量导入"
        );
        awardService.createByAdmin(request, operatorUserId);
    }

    private UserAccount createUser(String email, String password, UserRole role, String defaultPassword) {
        UserAccount user = new UserAccount();
        user.setEmail(email.trim().toLowerCase(Locale.ROOT));
        user.setPasswordHash(passwordEncoder.encode(isBlank(password) ? defaultPassword : password));
        user.setRole(role);
        user.setStatus(AccountStatus.NORMAL);
        return userRepository.save(user);
    }

    private void saveError(UUID jobId, int rowIndex, String fieldName, String message, String rawValue) {
        ImportErrorRow error = new ImportErrorRow();
        error.setJobId(jobId);
        error.setRowIndex(rowIndex);
        error.setFieldName(fieldName);
        error.setMessage(message);
        error.setRawValue(rawValue);
        importErrorRepository.save(error);
    }

    private byte[] workbookBytes(String sheetName, List<String> headers, List<List<String>> rows) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }
            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.createRow(r + 1);
                List<String> values = rows.get(r);
                for (int c = 0; c < values.size(); c++) {
                    row.createCell(c).setCellValue(values.get(c));
                }
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw ApiException.badRequest("生成 Excel 失败：" + exception.getMessage());
        }
    }

    private String requireType(String type) {
        if (!HEADERS.containsKey(type)) {
            throw ApiException.badRequest("导入导出类型仅支持 students、teachers、competitions、awards");
        }
        return type;
    }

    private String read(Row row, int index, DataFormatter formatter) {
        return formatter.formatCellValue(row.getCell(index)).trim();
    }

    private String requireCell(Row row, int index, DataFormatter formatter, String field) {
        String value = read(row, index, formatter);
        if (isBlank(value)) {
            throw ApiException.badRequest(field + "不能为空");
        }
        return value;
    }

    private CompetitionGrade parseCompetitionGrade(String value) {
        return parseEnum(value, CompetitionGrade.class);
    }

    private AwardLevel parseAwardLevel(String value) {
        return parseEnum(value, AwardLevel.class);
    }

    private AwardSubjectType parseSubjectType(String value) {
        return parseEnum(value, AwardSubjectType.class);
    }

    private AuditStatus parseAuditStatus(String value) {
        return isBlank(value) ? AuditStatus.APPROVED : parseEnum(value, AuditStatus.class);
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> type) {
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(value)) {
                return constant;
            }
            try {
                Object label = constant.getClass().getMethod("getLabel").invoke(constant);
                if (String.valueOf(label).equals(value)) {
                    return constant;
                }
            } catch (ReflectiveOperationException ignored) {
                // Some enums have no label; name matching above is enough.
            }
        }
        throw ApiException.badRequest("枚举值不支持：" + value);
    }

    private LocalDate parseDate(String value) {
        return isBlank(value) ? null : LocalDate.parse(value);
    }

    private List<UUID> parseAdvisorIds(String teacherNos) {
        if (isBlank(teacherNos)) {
            return List.of();
        }
        return Arrays.stream(teacherNos.split("[,，]"))
                .map(String::trim)
                .filter(no -> !no.isBlank())
                .map(no -> teacherRepository.findByTeacherNo(no)
                        .orElseThrow(() -> ApiException.badRequest("指导老师工号不存在：" + no))
                        .getId())
                .toList();
    }

    private CompetitionTrack findTrack(UUID competitionId, String name) {
        return trackRepository.findByCompetitionIdAndNameIgnoreCase(competitionId, name)
                .orElseThrow(() -> ApiException.badRequest("赛道不存在或不属于该竞赛：" + name));
    }

    private String emailOf(UUID userId) {
        return userRepository.findById(userId).map(UserAccount::getEmail).orElse("");
    }

    private String nameOfCompetition(UUID id) {
        return id == null ? "" : competitionRepository.findById(id).map(Competition::getName).orElse("");
    }

    private String nameOfTrack(UUID id) {
        return id == null ? "" : trackRepository.findById(id).map(CompetitionTrack::getName).orElse("");
    }

    private String studentNoOf(UUID studentId) {
        return studentId == null ? "" : studentRepository.findById(studentId).map(StudentProfile::getStudentNo).orElse("");
    }

    private String teacherNoOf(UUID teacherId) {
        return teacherId == null ? "" : teacherRepository.findById(teacherId).map(TeacherProfile::getTeacherNo).orElse("");
    }

    private String subjectValueOf(AwardSubjectType subjectType, UUID studentId, UUID teacherId, String teamName) {
        return switch (subjectType) {
            case STUDENT -> studentNoOf(studentId);
            case TEACHER -> teacherNoOf(teacherId);
            case TEAM -> safe(teamName);
        };
    }

    private String advisorNos(UUID awardId) {
        return advisorRepository.findByAwardId(awardId).stream()
                .map(AwardAdvisor::getTeacherId)
                .map(this::teacherNoOf)
                .filter(no -> !no.isBlank())
                .collect(Collectors.joining(","));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
