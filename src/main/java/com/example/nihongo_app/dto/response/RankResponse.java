package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RankResponse {

    Long rankId;
    String name;
    Integer minExpRequired;
    Integer orderIndex;
}
