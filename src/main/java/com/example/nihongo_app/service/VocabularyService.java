package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.request.VocabularyRequest;
import com.example.nihongo_app.dto.response.VocabularyAdminResponse;

public interface VocabularyService {

    VocabularyAdminResponse createVocabulary(VocabularyRequest request);

    VocabularyAdminResponse updateVocabulary(Long id, VocabularyRequest request);

    void deleteVocabulary(Long id);
}
