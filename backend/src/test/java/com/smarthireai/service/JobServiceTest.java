package com.smarthireai.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.smarthireai.dto.CreateJobRequest;
import com.smarthireai.entity.Job;
import com.smarthireai.entity.User;
import com.smarthireai.repository.JobRepository;
import com.smarthireai.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
class JobServiceTest {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        jobRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createJobAssociatesTheAuthenticatedRecruiter() {
        userRepository.save(new User(
                "recruiter@example.com",
                "encoded-password",
                "Recruiter User",
                User.UserRole.RECRUITER
        ));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "recruiter@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_RECRUITER"))
        ));

        Job job = jobService.createJob(new CreateJobRequest(
                "Backend Engineer",
                "Smart Hire AI",
                List.of("Java", "Spring"),
                3,
                "Bachelor",
                "Casablanca, Morocco",
                "Engineering",
                "Full-time",
                "Hybrid",
                "$45k - $65k",
                java.time.LocalDate.of(2026, 7, 15),
                "Open"
        ));

        assertThat(job.getRecruiter().getEmail()).isEqualTo("recruiter@example.com");
        assertThat(job.getRecruiter()).isInstanceOf(User.class);
        assertThat(job.getLocation()).isEqualTo("Casablanca, Morocco");
        assertThat(job.getDepartment()).isEqualTo("Engineering");
        assertThat(job.getEmploymentType()).isEqualTo("Full-time");
        assertThat(job.getWorkMode()).isEqualTo("Hybrid");
        assertThat(job.getSalaryRange()).isEqualTo("$45k - $65k");
        assertThat(job.getApplicationDeadline()).isEqualTo(java.time.LocalDate.of(2026, 7, 15));
        assertThat(job.getStatus()).isEqualTo("Open");
    }
}
