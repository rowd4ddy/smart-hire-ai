package com.smarthireai.controller;

import com.smarthireai.entity.CvVersion;
import com.smarthireai.service.CvService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final CvService cvService;

    public FileUploadController(CvService cvService) {
        this.cvService = cvService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvVersion> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        CvVersion cvVersion = cvService.uploadCv(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(cvVersion);
    }
}