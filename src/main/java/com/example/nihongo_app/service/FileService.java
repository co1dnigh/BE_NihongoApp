package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.response.UploadResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    UploadResponse uploadImage(MultipartFile file);

    UploadResponse uploadAudio(MultipartFile file);

    List<UploadResponse> uploadImages(List<MultipartFile> files);

    List<UploadResponse> uploadAudios(List<MultipartFile> files);
}