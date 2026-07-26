package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.response.UploadResponse;
import com.example.nihongo_app.service.FileService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final Set<String> ALLOWED_AUDIO_TYPES = Set.of(
            "audio/mpeg",
            "audio/mp3",
            "audio/wav",
            "audio/ogg",
            "audio/m4a",
            "audio/aac"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${app.file.upload-dir:uploads/}")
    private String uploadDir;

    @Override
    public UploadResponse uploadImage(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES, "image");
        return storeFile(file, "images");
    }

    @Override
    public UploadResponse uploadAudio(MultipartFile file) {
        validateFile(file, ALLOWED_AUDIO_TYPES, "audio");
        return storeFile(file, "audios");
    }

    @Override
    public List<UploadResponse> uploadImages(List<MultipartFile> files) {
        return uploadBatch(files, "images", ALLOWED_IMAGE_TYPES);
    }

    @Override
    public List<UploadResponse> uploadAudios(List<MultipartFile> files) {
        return uploadBatch(files, "audios", ALLOWED_AUDIO_TYPES);
    }

    private List<UploadResponse> uploadBatch(List<MultipartFile> files, String subFolder,
                                              Set<String> allowedTypes) {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No files provided");
        }
        List<UploadResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFile(file, allowedTypes, subFolder);
            responses.add(storeFile(file, subFolder));
        }
        return responses;
    }

    private void validateFile(MultipartFile file, Set<String> allowedTypes, String typeName) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid file type for " + typeName + ". Allowed: " + allowedTypes);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File size exceeds the maximum limit of 5MB");
        }
    }

    private UploadResponse storeFile(MultipartFile file, String subFolder) {
        String originalFilename = file.getOriginalFilename();
        String safeFilename = sanitizeFileName(originalFilename);

        try {
            Path uploadPath = Paths.get(uploadDir, subFolder).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            Path targetLocation = uploadPath.resolve(safeFilename);

            if (Files.exists(targetLocation)) {
                safeFilename = renameIfExists(safeFilename, uploadPath);
                targetLocation = uploadPath.resolve(safeFilename);
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/" + subFolder + "/" + safeFilename;

            log.info("File uploaded successfully: {} -> {} ({} bytes)",
                    subFolder, fileUrl, file.getSize());

            return UploadResponse.builder()
                    .fileName(safeFilename)
                    .fileUrl(fileUrl)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .build();

        } catch (IOException ex) {
            log.error("Could not store file: {}", safeFilename, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not store file. Please try again later");
        }
    }

    private String sanitizeFileName(String filename) {
        if (filename == null || filename.isBlank()) {
            return "unknown_file";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String renameIfExists(String filename, Path uploadPath) throws IOException {
        String name;
        String extension;
        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex > 0) {
            name = filename.substring(0, dotIndex);
            extension = filename.substring(dotIndex);
        } else {
            name = filename;
            extension = "";
        }

        int counter = 1;
        String newFilename = filename;
        while (Files.exists(uploadPath.resolve(newFilename))) {
            newFilename = name + "_" + counter + extension;
            counter++;
        }

        return newFilename;
    }
}