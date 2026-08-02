package com.example.nihongo_app.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EnergyResponse {
    Integer currentEnergy;
    Integer maxEnergy;
    String lastRecoveryDate;
}
