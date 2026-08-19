package com.example.nihongo_app.dto.request;

import com.example.nihongo_app.entity.Character.CharacterType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateAlphabetRequest {

    @NotBlank
    private String symbol;

    @NotBlank
    private String romaji;

    @NotNull
    private CharacterType type;

    @NotBlank
    private String groupName;

    private String audioUrl;
    private String strokeOrderData;

    @NotNull
    @Min(0)
    private Integer orderIndex;
}