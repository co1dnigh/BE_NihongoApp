package com.example.nihongo_app.dto.response;

import com.example.nihongo_app.entity.QuestDefinition.QuestType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QuestResponse {

    Long questId;
    String title;
    QuestType questType;
    Integer currentProgress;
    Integer targetValue;
    Boolean completed;
}
