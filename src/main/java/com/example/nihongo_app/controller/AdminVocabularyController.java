package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.request.VocabularyRequest;
import com.example.nihongo_app.dto.response.VocabularyAdminResponse;
import com.example.nihongo_app.service.VocabularyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/vocabularies")
@RequiredArgsConstructor
public class AdminVocabularyController {

    private final VocabularyService vocabularyService;

    @PostMapping
    public ResponseEntity<VocabularyAdminResponse> createVocabulary(@Valid @RequestBody VocabularyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vocabularyService.createVocabulary(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VocabularyAdminResponse> updateVocabulary(@PathVariable Long id,
                                                                    @Valid @RequestBody VocabularyRequest request) {
        return ResponseEntity.ok(vocabularyService.updateVocabulary(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVocabulary(@PathVariable Long id) {
        vocabularyService.deleteVocabulary(id);
        return ResponseEntity.noContent().build();
    }
}
