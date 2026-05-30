package com.smarthireai.service;

import com.smarthireai.dto.CvParseResult;
import com.smarthireai.entity.AppUser;
import com.smarthireai.entity.Candidate;
import com.smarthireai.entity.CvVersion;
import com.smarthireai.entity.UploadedFile;
import com.smarthireai.entity.User;
import com.smarthireai.repository.AppUserRepository;
import com.smarthireai.repository.CvVersionRepository;
import com.smarthireai.repository.UserRepository;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CvService {

    private static final Logger log = LoggerFactory.getLogger(CvService.class);

    private final CvVersionRepository cvVersionRepository;
    private final UserRepository userRepository;
    private final AppUserRepository appUserRepository;
    private final FileUploadService fileUploadService;
    private final CandidateService candidateService;
    private final AiServiceClient aiServiceClient;

    public CvService(
            CvVersionRepository cvVersionRepository,
            UserRepository userRepository,
            AppUserRepository appUserRepository,
            FileUploadService fileUploadService,
            CandidateService candidateService,
            AiServiceClient aiServiceClient
    ) {
        this.cvVersionRepository = cvVersionRepository;
        this.userRepository = userRepository;
        this.appUserRepository = appUserRepository;
        this.fileUploadService = fileUploadService;
        this.candidateService = candidateService;
        this.aiServiceClient = aiServiceClient;
    }

    public List<CvVersion> getMyCvVersions() {
        User candidate = getAuthenticatedUser();
        return cvVersionRepository.findByCandidateOrderByVersionNumberDesc(candidate);
    }

    @Transactional
    public CvVersion uploadCv(MultipartFile file) throws IOException {
        AppUser appUser = getAuthenticatedAppUser();
        User candidate = getAuthenticatedUser();

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CV file is required");
        }

        UploadedFile uploadedFile = fileUploadService.uploadFileWithoutSaving(file);
        byte[] fileBytes = file.getBytes();
        tryUpdateCandidateProfileFromCv(appUser, fileBytes, uploadedFile.getFileName());

        int nextVersion = cvVersionRepository.findTopByCandidateOrderByVersionNumberDesc(candidate)
                .map(CvVersion::getVersionNumber)
                .orElse(0) + 1;

        for (CvVersion version : cvVersionRepository.findByCandidateOrderByVersionNumberDesc(candidate)) {
            version.setActive(false);
        }

        CvVersion cvVersion = new CvVersion();
        cvVersion.setCandidate(candidate);
        cvVersion.setFileName(uploadedFile.getFileName());
        cvVersion.setFileUrl(uploadedFile.getFileUrl());
        cvVersion.setFileType(uploadedFile.getFileType());
        cvVersion.setFileSize(uploadedFile.getFileSize());
        cvVersion.setVersionNumber(nextVersion);
        cvVersion.setActive(true);

        return cvVersionRepository.save(cvVersion);
    }

    private void tryUpdateCandidateProfileFromCv(AppUser user, byte[] pdfBytes, String fileName) {
        if (user == null) {
            return;
        }

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

    private AppUser getAuthenticatedAppUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        String email = authentication.getName();
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"));
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found"));

        if (user.getRole() != User.UserRole.CANDIDATE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Candidate role is required");
        }

        return user;
    }
}