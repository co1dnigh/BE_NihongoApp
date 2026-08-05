package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChestStatusResponse {

    Boolean available;
    Boolean alreadyOpenedToday;
    Integer questsCompleted;
    Integer questsRequired;
}
