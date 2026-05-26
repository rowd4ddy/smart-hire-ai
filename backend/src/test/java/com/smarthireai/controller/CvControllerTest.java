package com.smarthireai.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.smarthireai.repository.CvVersionRepository;
import com.smarthireai.repository.UserRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CvControllerTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Autowired
    private CvVersionRepository cvVersionRepository;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    @AfterEach
    void tearDown() {
        cvVersionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void candidateCanUploadCvVersionsAndListHistory() throws Exception {
        String token = registerAndGetToken(
                "Candidate User",
                "candidate-cv@example.com",
                "CANDIDATE"
        );

        HttpResponse<String> firstUpload = uploadCv(token, "candidate-cv-v1.pdf", "PDF version 1");
        HttpResponse<String> secondUpload = uploadCv(token, "candidate-cv-v2.pdf", "PDF version 2");
        HttpResponse<String> history = get("/api/candidate/cvs", token);

        assertThat(firstUpload.statusCode()).isEqualTo(201);
        assertThat(firstUpload.body()).contains("\"versionNumber\":1");
        assertThat(firstUpload.body()).contains("\"active\":true");
        assertThat(firstUpload.body()).contains("\"fileName\":\"candidate-cv-v1.pdf\"");

        assertThat(secondUpload.statusCode()).isEqualTo(201);
        assertThat(secondUpload.body()).contains("\"versionNumber\":2");
        assertThat(secondUpload.body()).contains("\"active\":true");

        assertThat(history.statusCode()).isEqualTo(200);
        assertThat(history.body()).contains("\"fileName\":\"candidate-cv-v2.pdf\"");
        assertThat(history.body()).contains("\"fileName\":\"candidate-cv-v1.pdf\"");
        assertThat(history.body().indexOf("candidate-cv-v2.pdf")).isLessThan(history.body().indexOf("candidate-cv-v1.pdf"));
    }

    @Test
    void recruiterCannotUploadCandidateCv() throws Exception {
        String token = registerAndGetToken(
                "Recruiter User",
                "recruiter-cv@example.com",
                "RECRUITER"
        );

        HttpResponse<String> response = uploadCv(token, "recruiter-cv.pdf", "not allowed");

        assertThat(response.statusCode()).isIn(401, 403);
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

    private HttpResponse<String> uploadCv(String token, String fileName, String content) throws Exception {
        String boundary = "----SmartHireBoundary";
        String body = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
                + "Content-Type: application/pdf\r\n\r\n"
                + content + "\r\n"
                + "--" + boundary + "--\r\n";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/candidate/cvs"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
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

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @TestConfiguration
    static class S3TestConfiguration {

        @Bean
        @Primary
        S3Client mockS3Client() {
            S3Client s3Client = mock(S3Client.class);
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());
            return s3Client;
        }
    }
}
