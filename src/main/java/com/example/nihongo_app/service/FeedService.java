package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.response.CursorPageResponse;
import com.example.nihongo_app.dto.response.PostResponse;

public interface FeedService {

    /** Feed = bài của chính mình + những người đang follow, mới nhất trước, cursor-based. */
    CursorPageResponse<PostResponse> getFeed(Long userId, String cursor, int size);
}
