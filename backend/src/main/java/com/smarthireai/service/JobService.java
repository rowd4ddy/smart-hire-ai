package com.smarthireai.service;

import com.smarthireai.dto.CreateJobRequest;
import com.smarthireai.entity.Job;
import com.smarthireai.entity.User;
import com.smarthireai.repository.JobRepository;
import com.smarthireai.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobService(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job createJob(CreateJobRequest request) {
        User recruiter = getAuthenticatedRecruiter();
        
        Job job = new Job(
                recruiter,
                request.title(),
                request.company(),
                new ArrayList<>(request.requiredSkills() == null ? List.of() : request.requiredSkills()),
                request.minimumExperienceYears(),
                request.educationLevel(),
                request.location(),
                request.department(),
                request.employmentType(),
                request.workMode(),
                request.salaryRange(),
                request.applicationDeadline(),
                request.status() == null || request.status().isBlank() ? "Open" : request.status()
        );

        return jobRepository.save(job);
    }

    private User getAuthenticatedRecruiter() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        String email = authentication.getName();

        User recruiter = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found"));

        if (recruiter.getRole() != User.UserRole.RECRUITER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Recruiter role is required");
        }

        return recruiter;
    }
}
