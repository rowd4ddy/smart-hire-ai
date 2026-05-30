package com.smarthireai.service;

import com.smarthireai.dto.CvParseResult;
import com.smarthireai.dto.MatchResult;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AiServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    public CvParseResult parseCv(byte[] pdfBytes, String fileName) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(pdfBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            return restTemplate.postForObject(aiServiceUrl + "/parse-cv", request, CvParseResult.class);
        } catch (Exception e) {
            log.warn("AI service parse-cv call failed: {}", e.getMessage());
            return null;
        }
    }

    public MatchResult computeMatch(Map<String, Object> candidate, Map<String, Object> job) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "candidate", candidate,
                    "job", job
            );
            return restTemplate.postForObject(aiServiceUrl + "/match", requestBody, MatchResult.class);
        } catch (Exception e) {
            log.warn("AI service match call failed: {}", e.getMessage());
            return null;
        }
    }
}
