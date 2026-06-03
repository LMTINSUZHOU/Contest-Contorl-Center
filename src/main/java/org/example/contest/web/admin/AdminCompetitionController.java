package org.example.contest.web.admin;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.example.contest.common.ApiException;
import org.example.contest.domain.Competition;
import org.example.contest.domain.CompetitionTrack;
import org.example.contest.repository.CompetitionRepository;
import org.example.contest.repository.CompetitionTrackRepository;
import org.example.contest.web.dto.ManagementDtos;
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
public class AdminCompetitionController {
    private final CompetitionRepository competitionRepository;
    private final CompetitionTrackRepository trackRepository;

    public AdminCompetitionController(
            CompetitionRepository competitionRepository,
            CompetitionTrackRepository trackRepository
    ) {
        this.competitionRepository = competitionRepository;
        this.trackRepository = trackRepository;
    }

    @GetMapping("/competitions")
    public List<Competition> competitions(@RequestParam(required = false) String q) {
        return competitionRepository.findAll().stream()
                .filter(competition -> contains(competition.getName(), q) || contains(competition.getOrganizer(), q))
                .toList();
    }

    @PostMapping("/competitions")
    public Competition createCompetition(@Valid @RequestBody ManagementDtos.CompetitionRequest request) {
        if (competitionRepository.existsByNameIgnoreCase(request.name())) {
            throw ApiException.badRequest("竞赛名称已存在");
        }
        Competition competition = new Competition();
        applyCompetition(competition, request);
        return competitionRepository.save(competition);
    }

    @PutMapping("/competitions/{id}")
    @Transactional
    public Competition updateCompetition(@PathVariable UUID id, @Valid @RequestBody ManagementDtos.CompetitionRequest request) {
        Competition competition = competition(id);
        competitionRepository.findByNameIgnoreCase(request.name())
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw ApiException.badRequest("竞赛名称已存在");
                });
        applyCompetition(competition, request);
        return competition;
    }

    @DeleteMapping("/competitions/{id}")
    public void deleteCompetition(@PathVariable UUID id) {
        competitionRepository.delete(competition(id));
    }

    @GetMapping("/competitions/{id}/tracks")
    public List<CompetitionTrack> tracksByCompetition(@PathVariable UUID id) {
        return trackRepository.findByCompetitionIdOrderByNameAsc(id);
    }

    @GetMapping("/competition-tracks")
    public List<CompetitionTrack> tracks(@RequestParam(required = false) UUID competitionId) {
        return competitionId == null ? trackRepository.findAll() : trackRepository.findByCompetitionIdOrderByNameAsc(competitionId);
    }

    @PostMapping("/competition-tracks")
    public CompetitionTrack createTrack(@Valid @RequestBody ManagementDtos.CompetitionTrackRequest request) {
        competition(request.competitionId());
        trackRepository.findByCompetitionIdAndNameIgnoreCase(request.competitionId(), request.name())
                .ifPresent(found -> {
                    throw ApiException.badRequest("同一竞赛下赛道名称已存在");
                });
        CompetitionTrack track = new CompetitionTrack();
        applyTrack(track, request);
        return trackRepository.save(track);
    }

    @PutMapping("/competition-tracks/{id}")
    @Transactional
    public CompetitionTrack updateTrack(@PathVariable UUID id, @Valid @RequestBody ManagementDtos.CompetitionTrackRequest request) {
        competition(request.competitionId());
        CompetitionTrack track = track(id);
        trackRepository.findByCompetitionIdAndNameIgnoreCase(request.competitionId(), request.name())
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw ApiException.badRequest("同一竞赛下赛道名称已存在");
                });
        applyTrack(track, request);
        return track;
    }

    @DeleteMapping("/competition-tracks/{id}")
    public void deleteTrack(@PathVariable UUID id) {
        trackRepository.delete(track(id));
    }

    private Competition competition(UUID id) {
        return competitionRepository.findById(id).orElseThrow(() -> ApiException.notFound("竞赛不存在"));
    }

    private CompetitionTrack track(UUID id) {
        return trackRepository.findById(id).orElseThrow(() -> ApiException.notFound("竞赛赛道不存在"));
    }

    private void applyCompetition(Competition competition, ManagementDtos.CompetitionRequest request) {
        competition.setName(request.name());
        competition.setDefaultGrade(request.defaultGrade());
        competition.setOrganizer(request.organizer());
        competition.setCoOrganizer(request.coOrganizer());
        competition.setDescription(request.description());
        competition.setWebsiteUrl(request.websiteUrl());
        competition.setEnabled(request.enabled() == null || request.enabled());
    }

    private void applyTrack(CompetitionTrack track, ManagementDtos.CompetitionTrackRequest request) {
        track.setCompetitionId(request.competitionId());
        track.setName(request.name());
        track.setEnabled(request.enabled() == null || request.enabled());
    }

    private boolean contains(String value, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        return value != null && value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }
}
