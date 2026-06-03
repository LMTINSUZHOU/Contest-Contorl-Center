package org.example.contest.web;

import java.util.List;
import java.util.UUID;
import org.example.contest.domain.Competition;
import org.example.contest.domain.CompetitionTrack;
import org.example.contest.domain.TeacherProfile;
import org.example.contest.repository.CompetitionRepository;
import org.example.contest.repository.CompetitionTrackRepository;
import org.example.contest.repository.TeacherProfileRepository;
import org.example.contest.repository.TeamRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final CompetitionRepository competitionRepository;
    private final CompetitionTrackRepository trackRepository;
    private final TeacherProfileRepository teacherRepository;
    private final TeamRepository teamRepository;

    public CatalogController(
            CompetitionRepository competitionRepository,
            CompetitionTrackRepository trackRepository,
            TeacherProfileRepository teacherRepository,
            TeamRepository teamRepository
    ) {
        this.competitionRepository = competitionRepository;
        this.trackRepository = trackRepository;
        this.teacherRepository = teacherRepository;
        this.teamRepository = teamRepository;
    }

    @GetMapping("/competitions")
    public List<Competition> competitions() {
        return competitionRepository.findAll().stream().filter(Competition::isEnabled).toList();
    }

    @GetMapping("/tracks")
    public List<CompetitionTrack> tracks(@RequestParam UUID competitionId) {
        return trackRepository.findByCompetitionIdOrderByNameAsc(competitionId).stream()
                .filter(CompetitionTrack::isEnabled)
                .toList();
    }

    @GetMapping("/teachers")
    public List<TeacherProfile> teachers() {
        return teacherRepository.findAll();
    }

    @GetMapping("/teams")
    public List<SimpleTeam> teams() {
        return teamRepository.findAll().stream().map(team -> new SimpleTeam(team.getId(), team.getName())).toList();
    }

    public record SimpleTeam(UUID id, String name) {
    }

}
