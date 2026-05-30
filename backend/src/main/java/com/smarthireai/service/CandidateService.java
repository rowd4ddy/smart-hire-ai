package com.smarthireai.service;

import com.smarthireai.dto.CreateCandidateRequest;
import com.smarthireai.entity.AppUser;
import com.smarthireai.entity.Candidate;
import com.smarthireai.repository.CandidateRepository;
import com.smarthireai.repository.AppUserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final AppUserRepository appUserRepository;

    public CandidateService(CandidateRepository candidateRepository,
                            AppUserRepository appUserRepository) {
        this.candidateRepository = candidateRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    public Candidate createCandidate(CreateCandidateRequest request) {
        AppUser user = getAuthenticatedUser();

        if (candidateRepository.findByUser(user).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Candidate profile already exists for this user");
        }

        Candidate candidate = new Candidate(
                user,
                new ArrayList<>(request.skills() == null ? List.of() : request.skills()),
                request.experienceYears(),
                request.educationLevel()
        );

        return candidateRepository.save(candidate);
    }

    public Candidate getOrCreateCandidateForUser(AppUser user) {
        return candidateRepository.findByUser(user).orElseGet(() -> {
            Candidate candidate = new Candidate(user, new ArrayList<>(), 0, "UNKNOWN");
            return candidateRepository.save(candidate);
        });
    }

    public Candidate saveCandidate(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    private AppUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        String email = authentication.getName();
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"));
    }
}
