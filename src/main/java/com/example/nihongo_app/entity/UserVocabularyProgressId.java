package com.example.nihongo_app.entity;

import lombok.*;

import java.io.Serializable;

/** Khoá chính ghép (user, vocabulary) của {@link UserVocabularyProgress}. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserVocabularyProgressId implements Serializable {
    private Long userId;
    private Long vocabularyId;
}
