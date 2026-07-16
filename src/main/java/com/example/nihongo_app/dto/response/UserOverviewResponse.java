package com.example.nihongo_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOverviewResponse {

    private Long id;
    private String displayName;
    private String avatarUrl;
    private Integer level;
}