package com.smarthireai.controller;

import com.smarthireai.dto.CandidateProfileDto;
import com.smarthireai.dto.CreateCandidateRequest;
import com.smarthireai.entity.AppUser;
import com.smarthireai.entity.Candidate;
import com.smarthireai.repository.AppUserRepository;
import com.smarthireai.service.CandidateService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;
    private final AppUserRepository appUserRepository;

    public CandidateController(CandidateService candidateService,
                               AppUserRepository appUserRepository) {
        this.candidateService = candidateService;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    public List<Candidate> getCandidates() {
        return candidateService.getAllCandidates();
    }

    @GetMapping("/me")
    public CandidateProfileDto getMyProfile(Authentication authentication) {
        AppUser user = appUserRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Candidate candidate = candidateService.getOrCreateCandidateForUser(user);

        return new CandidateProfileDto(
                candidate.getId(),
                user.getFullName(),
                user.getEmail(),
                candidate.getSkills(),
                candidate.getExperienceYears() == null ? 0 : candidate.getExperienceYears(),
                candidate.getEducationLevel() == null ? "UNKNOWN" : candidate.getEducationLevel()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Candidate createCandidate(@RequestBody CreateCandidateRequest request) {
        return candidateService.createCandidate(request);
    }
}
