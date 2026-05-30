package com.smarthireai.service;

import com.smarthireai.dto.MatchResult;
import com.smarthireai.dto.RankedCandidateDto;
import com.smarthireai.entity.Candidate;
import com.smarthireai.entity.Job;
import com.smarthireai.repository.CandidateRepository;
import com.smarthireai.repository.JobRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MatchService {

    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final AiServiceClient aiServiceClient;

    public MatchService(JobRepository jobRepository,
                        CandidateRepository candidateRepository,
                        AiServiceClient aiServiceClient) {
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.aiServiceClient = aiServiceClient;
    }

    public List<RankedCandidateDto> getRankedCandidatesForJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        List<Candidate> candidates = candidateRepository.findAll();

        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<String, Object> jobData = Map.of(
                "required_skills", job.getRequiredSkills(),
                "minimum_experience_years", job.getMinimumExperienceYears() == null ? 0 : job.getMinimumExperienceYears(),
                "education_level", job.getEducationLevel() == null ? "UNKNOWN" : job.getEducationLevel()
        );

        return candidates.stream()
                .map(candidate -> scoreCandidate(candidate, jobData))
                .sorted(Comparator.comparingDouble(RankedCandidateDto::finalScore).reversed())
                .toList();
    }

    private RankedCandidateDto scoreCandidate(Candidate candidate, Map<String, Object> jobData) {
        Map<String, Object> candidateData = Map.of(
                "skills", candidate.getSkills(),
                "experience_years", candidate.getExperienceYears() == null ? 0 : candidate.getExperienceYears(),
                "education_level", candidate.getEducationLevel() == null ? "UNKNOWN" : candidate.getEducationLevel()
        );

        MatchResult result = aiServiceClient.computeMatch(candidateData, jobData);

        double finalScore      = result != null ? result.finalScore()      : 0.0;
        double skillsScore     = result != null ? result.skillsScore()     : 0.0;
        double experienceScore = result != null ? result.experienceScore() : 0.0;
        double educationScore  = result != null ? result.educationScore()  : 0.0;
        List<String> missing   = result != null ? result.missingSkills()   : List.of();

        String fullName = candidate.getUser() != null ? candidate.getUser().getFullName() : "Unknown";
        String email = candidate.getUser() != null ? candidate.getUser().getEmail() : "";

        return new RankedCandidateDto(
                candidate.getId(),
                fullName,
                email,
                candidate.getSkills(),
                candidate.getExperienceYears() == null ? 0 : candidate.getExperienceYears(),
                candidate.getEducationLevel() == null ? "UNKNOWN" : candidate.getEducationLevel(),
                finalScore,
                skillsScore,
                experienceScore,
                educationScore,
                missing
        );
    }
}
