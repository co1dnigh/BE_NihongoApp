package com.example.nihongo_app.service.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Fisher-Yates shuffle dùng chung cho các service cần trộn thứ tự câu hỏi/đáp án trước khi
 * trả về FE (start bài học thường, bắt đầu phiên ôn lỗi sai). Dùng {@code Math.random()}
 * (không cần {@code SecureRandom} cho use case game).
 */
final class ShuffleUtil {

    private ShuffleUtil() {
    }

    static <T> List<T> shuffle(List<T> source) {
        List<T> list = new ArrayList<>(source);
        for (int i = list.size() - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            T tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
        return list;
    }
}
