package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.entity.UserVocabularyProgress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Khoá lại hành vi của lịch ôn ngắt quãng.
 *
 * <p>Thứ được canh chặt nhất là hai đầu của thang: từ mới phải quay lại NGAY trong cùng
 * phiên (chứ không hẹn tới mai rồi mất hút), và từ đã thuộc phải giãn ra theo cấp số
 * nhân (chứ không lặp lại dày đặc làm người học phát chán).</p>
 */
class Sm2SchedulerTest {

    private final Sm2Scheduler scheduler = new Sm2Scheduler();
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 25, 9, 0);

    private UserVocabularyProgress fresh() {
        return scheduler.newProgress(1L, 100L);
    }

    /** Số phút từ NOW tới lịch ôn kế tiếp. */
    private long dueInMinutes(UserVocabularyProgress p) {
        return Duration.between(NOW, p.getNextDueAt()).toMinutes();
    }

    @Test
    @DisplayName("từ mới trả lời đúng thì quay lại sau 10 phút, không phải sang hôm sau")
    void newWordComesBackWithinTheSameSession() {
        UserVocabularyProgress p = fresh();

        scheduler.apply(p, scheduler.qualityOf(true), NOW);

        // SM-2 gốc cho 1 ngày ở bước này. Trên app di động thì người học rời phiên rồi
        // quên sạch — bước học ngắn là điểm sửa quan trọng nhất so với bản gốc.
        assertThat(dueInMinutes(p)).isEqualTo(10);
        assertThat(p.getRepetitions()).isEqualTo(1);
        assertThat(p.getFirstLearnedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("đúng liên tiếp thì khoảng cách giãn ra: 10 phút → 1 ngày → 6 ngày → nhân hệ số")
    void intervalsGrowExponentially() {
        UserVocabularyProgress p = fresh();
        LocalDateTime t = NOW;

        scheduler.apply(p, scheduler.qualityOf(true), t);
        assertThat(p.getIntervalMinutes()).isEqualTo(10);

        t = p.getNextDueAt();
        scheduler.apply(p, scheduler.qualityOf(true), t);
        assertThat(p.getIntervalMinutes()).isEqualTo(24 * 60);

        t = p.getNextDueAt();
        scheduler.apply(p, scheduler.qualityOf(true), t);
        assertThat(p.getIntervalMinutes()).isEqualTo(6 * 24 * 60);

        // Từ đây mới thật sự nhân với hệ số dễ nhớ.
        t = p.getNextDueAt();
        int before = p.getIntervalMinutes();
        scheduler.apply(p, scheduler.qualityOf(true), t);
        assertThat(p.getIntervalMinutes()).isGreaterThan(before);
        assertThat(p.getRepetitions()).isEqualTo(4);
    }

    @Test
    @DisplayName("trả lời sai thì reset chuỗi đúng và kéo từ về ôn lại ngay")
    void wrongAnswerResetsTheSchedule() {
        UserVocabularyProgress p = fresh();
        LocalDateTime t = NOW;
        for (int i = 0; i < 3; i++) {
            scheduler.apply(p, scheduler.qualityOf(true), t);
            t = p.getNextDueAt();
        }
        assertThat(p.getIntervalMinutes()).isEqualTo(6 * 24 * 60);

        scheduler.apply(p, scheduler.qualityOf(false), t);

        assertThat(p.getRepetitions()).isZero();
        assertThat(p.getIntervalMinutes()).isEqualTo(10);
        assertThat(p.getTotalWrong()).isEqualTo(1);
    }

    @Test
    @DisplayName("sai nhiều lần thì hệ số dễ nhớ tụt nhưng không xuống dưới sàn 1.3")
    void easeFactorHasAFloor() {
        UserVocabularyProgress p = fresh();
        LocalDateTime t = NOW;

        // Không có sàn thì hệ số tụt về gần 0 và từ này mắc kẹt vĩnh viễn ở khoảng cách
        // vài phút, chiếm sạch chỗ trong hàng đợi của mọi từ khác.
        for (int i = 0; i < 20; i++) {
            scheduler.apply(p, scheduler.qualityOf(false), t);
            t = t.plusMinutes(30);
        }

        assertThat(p.getEaseFactor()).isEqualByComparingTo(Sm2Scheduler.MIN_EASE);
        assertThat(p.getEaseFactor()).isGreaterThanOrEqualTo(new BigDecimal("1.300"));
    }

    @Test
    @DisplayName("từ khó bị ôn dày hơn từ dễ dù cùng số lần đúng")
    void hardWordsAreScheduledMoreOften() {
        UserVocabularyProgress easy = scheduler.newProgress(1L, 1L);
        UserVocabularyProgress hard = scheduler.newProgress(1L, 2L);

        // Từ "khó" là từ từng bị sai vài lần trước khi thuộc.
        LocalDateTime t = NOW;
        for (int i = 0; i < 3; i++) {
            scheduler.apply(hard, scheduler.qualityOf(false), t);
            t = t.plusMinutes(20);
        }

        // Rồi cả hai cùng đúng 5 lần liên tiếp.
        LocalDateTime te = NOW;
        for (int i = 0; i < 5; i++) {
            scheduler.apply(easy, scheduler.qualityOf(true), te);
            te = easy.getNextDueAt();
        }
        LocalDateTime th = t;
        for (int i = 0; i < 5; i++) {
            scheduler.apply(hard, scheduler.qualityOf(true), th);
            th = hard.getNextDueAt();
        }

        // Không ai gắn nhãn "từ khó" bằng tay — hệ số dễ nhớ tự lo việc đó.
        assertThat(hard.getEaseFactor()).isLessThan(easy.getEaseFactor());
        assertThat(hard.getIntervalMinutes()).isLessThan(easy.getIntervalMinutes());
    }

    @Test
    @DisplayName("lần gặp đầu tiên được ghi lại để dựng sổ tay và nhãn TỪ VỰNG MỚI")
    void firstLearnedAtIsStampedOnceAndNeverMoves() {
        UserVocabularyProgress p = fresh();
        assertThat(p.getFirstLearnedAt()).isNull();

        scheduler.apply(p, scheduler.qualityOf(true), NOW);
        LocalDateTime stamped = p.getFirstLearnedAt();
        assertThat(stamped).isEqualTo(NOW);

        scheduler.apply(p, scheduler.qualityOf(true), NOW.plusDays(3));
        assertThat(p.getFirstLearnedAt()).isEqualTo(stamped);
    }
}
