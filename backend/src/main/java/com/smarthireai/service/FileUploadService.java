package com.smarthireai.service;

import com.smarthireai.entity.UploadedFile;
import com.smarthireai.repository.UploadedFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileUploadService {

    private final S3Client s3Client;
    private final UploadedFileRepository uploadedFileRepository;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    public FileUploadService(S3Client s3Client, UploadedFileRepository uploadedFileRepository) {
        this.s3Client = s3Client;
        this.uploadedFileRepository = uploadedFileRepository;
    }

    public UploadedFile uploadFile(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String fileKey = UUID.randomUUID() + "-" + originalFileName;
        UploadedFile uploadedFile = uploadFile(file, fileKey);
        return uploadedFileRepository.save(uploadedFile);
    }

    public UploadedFile uploadFileWithoutSaving(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String fileKey = UUID.randomUUID() + "-" + originalFileName;
        return uploadFile(file, fileKey);
    }

    private UploadedFile uploadFile(MultipartFile file, String fileKey) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromBytes(file.getBytes())
        );

        String fileUrl = publicUrl + "/" + fileKey;
        String originalFileName = file.getOriginalFilename();

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setFileName(originalFileName);
        uploadedFile.setFileUrl(fileUrl);
        uploadedFile.setFileType(file.getContentType());
        uploadedFile.setFileSize(file.getSize());

        return uploadedFile;
    }
}
