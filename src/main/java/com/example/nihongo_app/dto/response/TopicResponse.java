package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TopicResponse {
    Long id;
    String title;
    String description;
    Integer orderIndex;
}
