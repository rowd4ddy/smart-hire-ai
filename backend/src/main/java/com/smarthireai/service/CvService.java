package com.smarthireai.service;

import com.smarthireai.entity.CvVersion;
import com.smarthireai.entity.UploadedFile;
import com.smarthireai.entity.User;
import com.smarthireai.repository.CvVersionRepository;
import com.smarthireai.repository.UserRepository;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CvService {

    private final CvVersionRepository cvVersionRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    public CvService(
            CvVersionRepository cvVersionRepository,
            UserRepository userRepository,
            FileUploadService fileUploadService
    ) {
        this.cvVersionRepository = cvVersionRepository;
        this.userRepository = userRepository;
        this.fileUploadService = fileUploadService;
    }

    public List<CvVersion> getMyCvVersions() {
        User candidate = getAuthenticatedCandidate();
        return cvVersionRepository.findByCandidateOrderByVersionNumberDesc(candidate);
    }

    public CvVersion uploadCv(MultipartFile file) throws IOException {
        User candidate = getAuthenticatedCandidate();

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CV file is required");
        }

        UploadedFile uploadedFile = fileUploadService.uploadFileWithoutSaving(file);
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

    private User getAuthenticatedCandidate() {
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
