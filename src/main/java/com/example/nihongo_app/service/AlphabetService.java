package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.request.CreateAlphabetRequest;
import com.example.nihongo_app.dto.request.SubmitAlphabetPracticeRequest;
import com.example.nihongo_app.dto.response.AlphabetMatrixItemResponse;
import com.example.nihongo_app.dto.response.AlphabetMatrixGroupResponse;
import com.example.nihongo_app.dto.response.AlphabetPracticeResponse;
import com.example.nihongo_app.dto.response.AlphabetResponse;
import com.example.nihongo_app.dto.response.SubmitAlphabetPracticeResponse;
import com.example.nihongo_app.entity.Character.CharacterType;
import java.util.List;

public interface AlphabetService {

    AlphabetResponse create(CreateAlphabetRequest request);

    List<AlphabetResponse> createBulk(List<CreateAlphabetRequest> requests);

    List<AlphabetMatrixGroupResponse> getMatrix(CharacterType type, Long userId);

    AlphabetPracticeResponse startPractice(Long userId);

    SubmitAlphabetPracticeResponse submitPractice(Long userId, SubmitAlphabetPracticeRequest request);
}