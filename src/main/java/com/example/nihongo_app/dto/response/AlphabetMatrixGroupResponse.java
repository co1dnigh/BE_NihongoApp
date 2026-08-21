package com.example.nihongo_app.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AlphabetMatrixGroupResponse {
    String groupName;
    List<AlphabetMatrixItemResponse> characters;
}