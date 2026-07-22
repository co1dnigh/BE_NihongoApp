package com.example.nihongo_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonAdminResponse {

    private Long id;
    private String jlptLevel;
    private String title;
    private Integer orderIndex;
}
