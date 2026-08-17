package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitSectionResponse {
    private Long sectionId;
    private Integer score;
    private Integer maxScore;
    private Long nextSectionId;
    private Integer remainingAttemptSeconds;
}
