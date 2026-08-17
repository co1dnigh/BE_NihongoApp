package com.example.nihongo_app.dto.request;

import java.util.List;
import lombok.Data;

/**
 * Request cho {@code POST /api/v1/reviews/mistakes/submit}.
 */
@Data
public class ReviewSubmitRequest {

    private List<AnswerItem> answers;
}
