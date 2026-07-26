package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.response.UploadResponse;
import com.example.nihongo_app.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // ============== SINGLE FILE ==============

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadImage(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "image", required = false) MultipartFile image,
            HttpServletRequest request) {
        MultipartFile payload = resolveOne(request, file, image);
        return ResponseEntity.ok(fileService.uploadImage(payload));
    }

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadAudio(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "audio", required = false) MultipartFile audio,
            HttpServletRequest request) {
        MultipartFile payload = resolveOne(request, file, audio);
        return ResponseEntity.ok(fileService.uploadAudio(payload));
    }

    // ============== MULTI FILE (BATCH) ==============

    @PostMapping(value = "/images/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<UploadResponse>> uploadImagesBatch(
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "file", required = false) List<MultipartFile> file,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpServletRequest request) {
        List<MultipartFile> payload = resolveMany(request, files, file, images);
        return ResponseEntity.ok(fileService.uploadImages(payload));
    }

    @PostMapping(value = "/audio/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<UploadResponse>> uploadAudiosBatch(
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "file", required = false) List<MultipartFile> file,
            @RequestParam(value = "audios", required = false) List<MultipartFile> audios,
            HttpServletRequest request) {
        List<MultipartFile> payload = resolveMany(request, files, file, audios);
        return ResponseEntity.ok(fileService.uploadAudios(payload));
    }

    // ============== HELPERS ==============

    private MultipartFile resolveOne(HttpServletRequest request, MultipartFile... candidates) {
        for (MultipartFile c : candidates) {
            if (c != null && !c.isEmpty()) {
                return c;
            }
        }
        if (request instanceof MultipartHttpServletRequest multipart) {
            List<MultipartFile> all = collectAllFiles(multipart);
            for (MultipartFile f : all) {
                if (f != null && !f.isEmpty()) {
                    log.warn("Upload fallback: dùng part '{}' ({} bytes) thay vì key mong đợi",
                            f.getName(), f.getSize());
                    return f;
                }
            }
        }
        log.error("Request không có multipart file hợp lệ. Content-Type={}",
                request.getContentType());
        throw new IllegalArgumentException("Multipart part 'file' is required (and must not be empty)");
    }

    /**
     * Gom tất cả file từ các key dự kiến + fallback quét toàn bộ part.
     * Postman nếu gõ cùng key cho nhiều dòng -> Spring tự gom thành List.
     * Nếu key khác (image/audio/file) -> fallback quét tất cả part.
     */
    private List<MultipartFile> resolveMany(HttpServletRequest request,
                                             List<MultipartFile>... candidates) {
        List<MultipartFile> result = new ArrayList<>();
        for (List<MultipartFile> c : candidates) {
            if (c != null) {
                for (MultipartFile f : c) {
                    if (f != null && !f.isEmpty()) {
                        result.add(f);
                    }
                }
            }
        }
        if (result.isEmpty() && request instanceof MultipartHttpServletRequest multipart) {
            List<MultipartFile> all = collectAllFiles(multipart);
            for (MultipartFile f : all) {
                if (f != null && !f.isEmpty()) {
                    log.warn("Upload batch fallback: dùng part '{}' ({} bytes)", f.getName(), f.getSize());
                    result.add(f);
                }
            }
        }
        if (result.isEmpty()) {
            log.error("Request batch không có multipart file hợp lệ. Content-Type={}",
                    request.getContentType());
            throw new IllegalArgumentException("At least one multipart file is required");
        }
        return result;
    }

    /**
     * Lấy TẤT CẢ file từ request, kể cả khi nhiều part có cùng tên.
     * getFileMap() chỉ trả về 1 file cho mỗi key, nên dùng getMultiFileMap().
     */
    private List<MultipartFile> collectAllFiles(MultipartHttpServletRequest multipart) {
        List<MultipartFile> all = new ArrayList<>();
        Map<String, MultipartFile> fileMap = multipart.getFileMap();
        for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
            MultipartFile f = entry.getValue();
            if (f != null && !f.isEmpty()) {
                all.add(f);
            }
        }
        // Nếu nghi ngờ có nhiều part cùng key, quét thêm getMultiFileMap()
        if (fileMap.size() < multipart.getMultiFileMap().values().stream()
                .mapToInt(List::size).sum()) {
            all.clear();
            for (List<MultipartFile> files : multipart.getMultiFileMap().values()) {
                for (MultipartFile f : files) {
                    if (f != null && !f.isEmpty()) {
                        all.add(f);
                    }
                }
            }
        }
        return all;
    }
}