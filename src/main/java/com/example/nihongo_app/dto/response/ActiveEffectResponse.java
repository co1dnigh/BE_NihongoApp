package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ActiveEffectResponse {
    String effectType;
    LocalDateTime expiresAt;
}
