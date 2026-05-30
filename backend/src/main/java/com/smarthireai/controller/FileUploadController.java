package com.smarthireai.controller;

import com.smarthireai.entity.UploadedFile;
import com.smarthireai.service.CvService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final CvService cvService;

    public FileUploadController(CvService cvService) {
        this.cvService = cvService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        UploadedFile uploadedFile = cvService.uploadCv(file);

        return ResponseEntity.ok(Map.of(
                "id", uploadedFile.getId(),
                "url", uploadedFile.getFileUrl(),
                "fileName", uploadedFile.getFileName(),
                "fileType", uploadedFile.getFileType(),
                "fileSize", uploadedFile.getFileSize()
        ));
    }
}