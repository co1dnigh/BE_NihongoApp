package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.response.RoadmapTopicResponse;
import java.util.List;

public interface RoadmapService {

    /**
     * Lấy toàn bộ lộ trình học cho user hiện tại, kèm trạng thái "thời gian thực"
     * của từng bài học (LOCKED / UNLOCKED / COMPLETED) và số sao đạt được (với TIMED_REVIEW).
     *
     * @param userId ID của user đang đăng nhập (lấy từ JWT thông qua {@code AppUserPrincipal}).
     * @return danh sách topic đang active, sắp xếp theo orderIndex; trong mỗi topic
     *         các bài học cũng đã được sắp xếp theo orderIndex.
     */
    List<RoadmapTopicResponse> getRoadmap(Long userId);
}
