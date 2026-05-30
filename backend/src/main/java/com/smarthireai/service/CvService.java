package com.smarthireai.service;

import com.smarthireai.dto.CvParseResult;
import com.smarthireai.entity.AppUser;
import com.smarthireai.entity.Candidate;
import com.smarthireai.entity.UploadedFile;
import com.smarthireai.repository.AppUserRepository;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CvService {

    private static final Logger log = LoggerFactory.getLogger(CvService.class);

    private final FileUploadService fileUploadService;
    private final CandidateService candidateService;
    private final AiServiceClient aiServiceClient;
    private final AppUserRepository appUserRepository;

    public CvService(FileUploadService fileUploadService,
                     CandidateService candidateService,
                     AiServiceClient aiServiceClient,
                     AppUserRepository appUserRepository) {
        this.fileUploadService = fileUploadService;
        this.candidateService = candidateService;
        this.aiServiceClient = aiServiceClient;
        this.appUserRepository = appUserRepository;
    }

    public UploadedFile uploadCv(MultipartFile file) throws IOException {
        AppUser user = getAuthenticatedCandidate();

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CV file is required");
        }

        byte[] fileBytes = file.getBytes();
        UploadedFile uploadedFile = fileUploadService.uploadFile(file);

        tryUpdateCandidateProfileFromCv(user, fileBytes, uploadedFile.getFileName());

        return uploadedFile;
    }

    private void tryUpdateCandidateProfileFromCv(AppUser user, byte[] pdfBytes, String fileName) {
        try {
            CvParseResult result = aiServiceClient.parseCv(pdfBytes, fileName);
            if (result == null) {
                log.warn("AI service returned null for user {}, skipping profile update", user.getEmail());
                return;
            }

            Candidate candidate = candidateService.getOrCreateCandidateForUser(user);

            if (result.skills() != null && !result.skills().isEmpty()) {
                candidate.setSkills(result.skills());
            }
            if (result.experienceYears() > 0) {
                candidate.setExperienceYears(result.experienceYears());
            }
            if (result.educationLevel() != null && !result.educationLevel().equals("UNKNOWN")) {
                candidate.setEducationLevel(result.educationLevel());
            }

            candidateService.saveCandidate(candidate);

            log.info("Updated candidate profile for {} — skills: {}, exp: {}y, edu: {}",
                    user.getEmail(), result.skills(), result.experienceYears(), result.educationLevel());
        } catch (Exception e) {
            log.error("Failed to update candidate profile from CV for user {}: {}",
                    user.getEmail(), e.getMessage());
        }
    }

    private AppUser getAuthenticatedCandidate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        String email = authentication.getName();
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"));

        if (user.getRole() != com.smarthireai.entity.Role.CANDIDATE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Candidate role is required");
        }

        return user;
    }
}
