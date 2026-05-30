package com.smarthireai.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.smarthireai.repository.JobRepository;
import com.smarthireai.repository.UserRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JobControllerTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    @AfterEach
    void tearDown() {
        jobRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void recruiterCanCreateJobWithJwtToken() throws Exception {
        String token = registerAndGetToken(
                "Recruiter User",
                "recruiter@example.com",
                "RECRUITER"
        );

        HttpResponse<String> response = postJson(
                "/api/jobs",
                """
                        {
                          "title": "Backend Engineer",
                          "company": "Smart Hire AI",
                          "requiredSkills": ["Java", "Spring"],
                          "minimumExperienceYears": 3,
                          "educationLevel": "Bachelor",
                          "location": "Casablanca, Morocco",
                          "department": "Engineering",
                          "employmentType": "Full-time",
                          "workMode": "Hybrid",
                          "salaryRange": "$45k - $65k",
                          "applicationDeadline": "2026-07-15",
                          "status": "Open"
                        }
                        """,
                token
        );

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains("\"title\":\"Backend Engineer\"");
        assertThat(response.body()).contains("\"company\":\"Smart Hire AI\"");
        assertThat(response.body()).contains("\"location\":\"Casablanca, Morocco\"");
        assertThat(response.body()).contains("\"employmentType\":\"Full-time\"");
        assertThat(response.body()).contains("\"workMode\":\"Hybrid\"");
        assertThat(response.body()).contains("\"applicationDeadline\":\"2026-07-15\"");
        assertThat(response.body()).doesNotContain("recruiter");

        HttpResponse<String> jobsResponse = get("/api/jobs");
        assertThat(jobsResponse.statusCode()).isEqualTo(200);
        assertThat(jobsResponse.body()).contains("\"title\":\"Backend Engineer\"");
        assertThat(jobsResponse.body()).contains("\"company\":\"Smart Hire AI\"");
        assertThat(jobsResponse.body()).contains("\"status\":\"Open\"");
    }

    @Test
    void candidateCannotCreateJob() throws Exception {
        String token = registerAndGetToken(
                "Candidate User",
                "candidate@example.com",
                "CANDIDATE"
        );

        HttpResponse<String> response = postJson(
                "/api/jobs",
                """
                        {
                          "title": "Backend Engineer",
                          "company": "Smart Hire AI",
                          "requiredSkills": ["Java", "Spring"],
                          "minimumExperienceYears": 3,
                          "educationLevel": "Bachelor",
                          "location": "Casablanca, Morocco",
                          "department": "Engineering",
                          "employmentType": "Full-time",
                          "workMode": "Hybrid",
                          "salaryRange": "$45k - $65k",
                          "applicationDeadline": "2026-07-15",
                          "status": "Open"
                        }
                        """,
                token
        );

        assertThat(response.statusCode()).isIn(401, 403);
        assertThat(jobRepository.count()).isZero();
    }

    private String registerAndGetToken(String fullName, String email, String role) throws Exception {
        HttpResponse<String> response = postJson(
                "/api/auth/register",
                """
                        {
                          "fullName": "%s",
                          "email": "%s",
                          "password": "password123",
                          "role": "%s"
                        }
                        """.formatted(fullName, email, role),
                null
        );

        assertThat(response.statusCode()).isEqualTo(201);
        String responseBody = response.body();
        int tokenStart = responseBody.indexOf("\"token\":\"") + 9;
        int tokenEnd = responseBody.indexOf('"', tokenStart);
        return responseBody.substring(tokenStart, tokenEnd);
    }

    private HttpResponse<String> postJson(String path, String body, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
