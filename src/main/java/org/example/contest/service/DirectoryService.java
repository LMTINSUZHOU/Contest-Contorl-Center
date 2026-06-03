package org.example.contest.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.example.contest.common.ApiException;
import org.example.contest.domain.Competition;
import org.example.contest.domain.CompetitionTrack;
import org.example.contest.domain.StudentProfile;
import org.example.contest.domain.TeacherProfile;
import org.example.contest.domain.enums.CompetitionGrade;
import org.example.contest.repository.CompetitionRepository;
import org.example.contest.repository.CompetitionTrackRepository;
import org.example.contest.repository.StudentProfileRepository;
import org.example.contest.repository.TeacherProfileRepository;
import org.springframework.stereotype.Service;

/**
 * 字典和基础资料解析服务，集中处理名称展示与竞赛等级覆盖规则。
 */
@Service
public class DirectoryService {
    private final CompetitionRepository competitionRepository;
    private final CompetitionTrackRepository trackRepository;
    private final StudentProfileRepository studentRepository;
    private final TeacherProfileRepository teacherRepository;

    public DirectoryService(
            CompetitionRepository competitionRepository,
            CompetitionTrackRepository trackRepository,
            StudentProfileRepository studentRepository,
            TeacherProfileRepository teacherRepository
    ) {
        this.competitionRepository = competitionRepository;
        this.trackRepository = trackRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    public Competition requireCompetition(UUID id) {
        return competitionRepository.findById(id).orElseThrow(() -> ApiException.notFound("竞赛不存在"));
    }

    public CompetitionTrack requireTrack(UUID competitionId, UUID trackId) {
        if (trackId == null) {
            throw ApiException.badRequest("赛道不能为空");
        }
        CompetitionTrack track = trackRepository.findById(trackId).orElseThrow(() -> ApiException.notFound("竞赛赛道不存在"));
        if (!competitionId.equals(track.getCompetitionId())) {
            throw ApiException.badRequest("所选赛道不属于当前竞赛");
        }
        return track;
    }

    public CompetitionGrade resolveCompetitionGrade(UUID competitionId) {
        return requireCompetition(competitionId).getDefaultGrade();
    }

    public String competitionName(UUID id) {
        return id == null ? null : competitionRepository.findById(id).map(Competition::getName).orElse(null);
    }

    public String trackName(UUID id) {
        return id == null ? null : trackRepository.findById(id).map(CompetitionTrack::getName).orElse(null);
    }

    public String studentName(UUID id) {
        return id == null ? null : studentRepository.findById(id).map(StudentProfile::getName).orElse(null);
    }

    public String teacherName(UUID id) {
        return id == null ? null : teacherRepository.findById(id).map(TeacherProfile::getName).orElse(null);
    }

    public Map<UUID, StudentProfile> studentsById(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return studentRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(StudentProfile::getId, Function.identity()));
    }

    public Map<UUID, TeacherProfile> teachersById(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return teacherRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(TeacherProfile::getId, Function.identity()));
    }
}
