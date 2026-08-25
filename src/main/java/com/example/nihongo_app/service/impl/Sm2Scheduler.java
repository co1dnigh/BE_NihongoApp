package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.entity.UserVocabularyProgress;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Lịch ôn tập ngắt quãng theo SM-2 (thuật toán Anki dùng), có sửa cho hợp app di động.
 *
 * <h3>Vì sao SM-2 chứ không phải Half-Life Regression của Duolingo</h3>
 * HLR (Settles &amp; Meeder, ACL 2016) là mô hình HỌC TỪ DỮ LIỆU: nó hồi quy "nửa đời trí
 * nhớ" từ 13 triệu lượt học thật. Không có khối dữ liệu đó thì mô hình không hội tụ —
 * đây là bài toán cold-start kinh điển. SM-2 cho ra lịch ôn tốt gần bằng mà chỉ cần một
 * công thức cố định, chạy được ngay từ người dùng đầu tiên. Khi nào log đủ lớn thì thay
 * riêng lớp này, phần còn lại của hệ thống không phải đụng tới.
 *
 * <h3>Sửa gì so với SM-2 gốc</h3>
 * <ol>
 *   <li><b>Đơn vị PHÚT, không phải ngày.</b> SM-2 gốc cho từ mới khoảng cách 1 ngày.
 *       Trên app di động, từ vừa gặp lần đầu mà hẹn tới mai là quá muộn: người học rời
 *       phiên rồi quên sạch, lần sau gặp lại như chưa từng học. Thêm "bước học"
 *       ({@link #LEARNING_STEPS_MINUTES}) 10 phút → 1 ngày trước khi tốt nghiệp sang
 *       khoảng cách dài, để từ mới quay lại NGAY trong cùng phiên.</li>
 *   <li><b>Không có điểm tự chấm 0–5.</b> Anki hỏi người dùng "dễ hay khó?". Bài học ở
 *       đây là trắc nghiệm, không hỏi được, nên chất lượng {@code q} suy ra từ dữ liệu
 *       sẵn có — xem {@link #qualityOf}.</li>
 * </ol>
 */
@Component
public class Sm2Scheduler {

    /** Hệ số dễ nhớ khởi tạo, theo đúng SM-2 gốc. */
    public static final BigDecimal INITIAL_EASE = new BigDecimal("2.500");

    /**
     * Sàn của hệ số dễ nhớ. Không có sàn này thì một từ bị sai nhiều lần sẽ tụt hệ số
     * về gần 0 và mắc kẹt vĩnh viễn ở khoảng cách vài phút, chiếm chỗ của mọi từ khác.
     */
    public static final BigDecimal MIN_EASE = new BigDecimal("1.300");

    /**
     * Các bước trong "giai đoạn học", tính bằng phút: gặp lại sau 10 phút, rồi sau 1 ngày.
     * Qua hết hai bước này mới tốt nghiệp sang khoảng cách dài.
     */
    private static final int[] LEARNING_STEPS_MINUTES = {10, 24 * 60};

    /** Khoảng cách ngay sau khi tốt nghiệp — 6 ngày, theo SM-2 gốc. */
    private static final int GRADUATED_INTERVAL_MINUTES = 6 * 24 * 60;

    /** Trả lời sai thì về bước học đầu tiên chứ không về 0 hẳn. */
    private static final int RELEARN_INTERVAL_MINUTES = LEARNING_STEPS_MINUTES[0];

    /** Ngưỡng {@code q} để tính là "nhớ được". Dưới ngưỡng là quên. */
    private static final int PASSING_QUALITY = 3;

    /** Chặn trên: quá một năm thì lịch ôn mất ý nghĩa thực tế. */
    private static final int MAX_INTERVAL_MINUTES = 365 * 24 * 60;

    /**
     * Suy ra chất lượng nhớ lại {@code q} (thang SM-2 0–5) từ những gì luồng trắc nghiệm
     * thật sự biết.
     *
     * <p>Chỉ dùng hai mốc chắc chắn: sai và đúng. Mốc thứ ba (đúng-mà-chật-vật) cần đo
     * thời gian trả lời từng câu — hiện client mới gửi tổng thời gian cả bài, nên chưa
     * phân biệt được. Khi nào có thì thêm nhánh ở đây, không phải sửa chỗ nào khác.</p>
     *
     * @param correct người học trả lời đúng hay không
     */
    public int qualityOf(boolean correct) {
        return correct ? 4 : 1;
    }

    /**
     * Áp một lần trả lời vào trạng thái ôn tập, cập nhật {@code progress} tại chỗ.
     *
     * @param progress trạng thái hiện tại (sẽ bị sửa)
     * @param quality  điểm 0–5 từ {@link #qualityOf}
     * @param now      mốc thời gian coi là "bây giờ"
     */
    public void apply(UserVocabularyProgress progress, int quality, LocalDateTime now) {
        boolean remembered = quality >= PASSING_QUALITY;

        if (progress.getFirstLearnedAt() == null) {
            progress.setFirstLearnedAt(now);
        }

        if (remembered) {
            progress.setRepetitions(progress.getRepetitions() + 1);
            progress.setTotalCorrect(progress.getTotalCorrect() + 1);
            progress.setIntervalMinutes(nextIntervalMinutes(progress, quality));
        } else {
            // Quên thì chuỗi đúng liên tiếp về 0 và từ quay lại hàng đợi gần.
            progress.setRepetitions(0);
            progress.setTotalWrong(progress.getTotalWrong() + 1);
            progress.setIntervalMinutes(RELEARN_INTERVAL_MINUTES);
        }

        progress.setEaseFactor(nextEase(progress.getEaseFactor(), quality));
        progress.setLastReviewedAt(now);
        progress.setNextDueAt(now.plusMinutes(progress.getIntervalMinutes()));
    }

    /**
     * Khoảng cách kế tiếp khi trả lời đúng.
     *
     * <p>Hai lần đúng đầu đi theo bước học cố định; từ lần thứ ba trở đi mới nhân với hệ
     * số dễ nhớ — đây chính là chỗ khoảng cách giãn ra theo cấp số nhân
     * (6 ngày → 15 ngày → 37 ngày → …) với từ dễ, và bò chậm với từ khó.</p>
     */
    private int nextIntervalMinutes(UserVocabularyProgress progress, int quality) {
        int reps = progress.getRepetitions();

        if (reps <= LEARNING_STEPS_MINUTES.length) {
            return LEARNING_STEPS_MINUTES[reps - 1];
        }
        if (reps == LEARNING_STEPS_MINUTES.length + 1) {
            return GRADUATED_INTERVAL_MINUTES;
        }

        BigDecimal ease = nextEase(progress.getEaseFactor(), quality);
        long grown = BigDecimal.valueOf(progress.getIntervalMinutes())
                .multiply(ease)
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
        return (int) Math.min(grown, MAX_INTERVAL_MINUTES);
    }

    /**
     * Công thức cập nhật hệ số dễ nhớ của SM-2:
     * {@code EF' = EF + (0.1 - (5-q) * (0.08 + (5-q) * 0.02))}.
     *
     * <p>Trả lời đúng ngon lành (q=5) thì hệ số nhích lên; đúng chật vật (q=3) thì tụt
     * nhẹ; sai thì tụt mạnh. Nhờ vậy từ nào người học liên tục vấp sẽ tự động được ôn
     * dày hơn từ dễ, mà không cần ai gắn nhãn "từ khó" bằng tay.</p>
     */
    private BigDecimal nextEase(BigDecimal current, int quality) {
        BigDecimal base = current == null ? INITIAL_EASE : current;
        double q = quality;
        double delta = 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02);
        BigDecimal next = base.add(BigDecimal.valueOf(delta))
                .setScale(3, RoundingMode.HALF_UP);
        return next.compareTo(MIN_EASE) < 0 ? MIN_EASE : next;
    }

    /** Trạng thái khởi tạo cho một từ người học chưa từng gặp. */
    public UserVocabularyProgress newProgress(Long userId, Long vocabularyId) {
        return UserVocabularyProgress.builder()
                .userId(userId)
                .vocabularyId(vocabularyId)
                .repetitions(0)
                .easeFactor(INITIAL_EASE)
                .intervalMinutes(0)
                .totalCorrect(0)
                .totalWrong(0)
                .build();
    }
}
