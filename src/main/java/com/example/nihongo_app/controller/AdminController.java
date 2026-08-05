package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.request.CreateAdminRequest;
import com.example.nihongo_app.dto.request.CreateLessonRequest;
import com.example.nihongo_app.dto.request.CreateQuestionRequest;
import com.example.nihongo_app.dto.request.CreateTopicRequest;
import com.example.nihongo_app.dto.response.AdminSummaryResponse;
import com.example.nihongo_app.dto.response.LessonResponse;
import com.example.nihongo_app.dto.response.LessonWithQuestionsResponse;
import com.example.nihongo_app.dto.response.QuestionResponse;
import com.example.nihongo_app.dto.response.QuestionWithOptionsResponse;
import com.example.nihongo_app.dto.response.TopicResponse;
import com.example.nihongo_app.dto.response.TopicWithLessonsResponse;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.service.AdminContentService;
import com.example.nihongo_app.service.UserService;
import com.example.nihongo_app.dto.request.UpdateProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Locale;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping({"/api/v1/admin", "/api/admin"})
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Quản trị nội dung (Topic/Lesson/Question) và user - chỉ role ADMIN")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminContentService adminContentService;
    private final UserService userService;

    // ================= TOPICS =================

    @GetMapping("/topics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Lấy danh sách tất cả Topic kèm Lesson")
    public ResponseEntity<List<TopicWithLessonsResponse>> getAllTopics() {
        return ResponseEntity.ok(adminContentService.getAllTopics());
    }

    @GetMapping("/topics/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Lấy chi tiết 1 Topic kèm Lesson")
    public ResponseEntity<TopicWithLessonsResponse> getTopicById(@PathVariable Long id) {
        return ResponseEntity.ok(adminContentService.getTopicById(id));
    }

    @PutMapping("/topics/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Cập nhật Topic")
    public ResponseEntity<TopicResponse> updateTopic(@PathVariable Long id,
                                                      @Valid @RequestBody CreateTopicRequest request) {
        return ResponseEntity.ok(adminContentService.updateTopic(id, request));
    }

    @DeleteMapping("/topics/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Xoá Topic")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        adminContentService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }

    // ĐÃ BỔ SUNG: API Tạo Topic mới
    @PostMapping("/topics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Tạo Topic mới")
    public ResponseEntity<TopicResponse> createTopic(@Valid @RequestBody CreateTopicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminContentService.createTopic(request));
    }

    // ================= LESSONS =================

    @GetMapping("/lessons")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Lấy danh sách tất cả Lesson")
    public ResponseEntity<List<LessonResponse>> getAllLessons() {
        return ResponseEntity.ok(adminContentService.getAllLessons());
    }

    @GetMapping("/lessons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Lấy chi tiết 1 Lesson kèm câu hỏi")
    public ResponseEntity<LessonWithQuestionsResponse> getLessonById(@PathVariable Long id) {
        return ResponseEntity.ok(adminContentService.getLessonById(id));
    }

    @PutMapping("/lessons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Cập nhật Lesson")
    public ResponseEntity<LessonResponse> updateLesson(@PathVariable Long id,
                                                        @Valid @RequestBody CreateLessonRequest request) {
        return ResponseEntity.ok(adminContentService.updateLesson(id, request));
    }

    @DeleteMapping("/lessons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Xoá Lesson")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        adminContentService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    // ĐÃ BỔ SUNG: API Tạo Lesson mới
    @PostMapping("/lessons")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Tạo Lesson mới trong 1 Topic")
    public ResponseEntity<LessonResponse> createLesson(@Valid @RequestBody CreateLessonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminContentService.createLesson(request));
    }

    // ================= QUESTIONS =================

    // ĐÃ BỔ SUNG: API Lấy danh sách Câu hỏi (Cho Frontend render bảng)
    @GetMapping("/questions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Lấy danh sách tất cả câu hỏi kèm đáp án")
    public ResponseEntity<List<QuestionWithOptionsResponse>> getAllQuestions() {
        return ResponseEntity.ok(adminContentService.getAllQuestions());
    }

    @GetMapping("/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Lấy chi tiết 1 câu hỏi kèm đáp án")
    public ResponseEntity<QuestionWithOptionsResponse> getQuestionById(@PathVariable Long id) {
        return ResponseEntity.ok(adminContentService.getQuestionById(id));
    }

    @PutMapping("/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Cập nhật câu hỏi")
    public ResponseEntity<QuestionResponse> updateQuestion(@PathVariable Long id,
                                                            @Valid @RequestBody CreateQuestionRequest request) {
        return ResponseEntity.ok(adminContentService.updateQuestion(id, request));
    }

    @DeleteMapping("/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Xoá câu hỏi")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        adminContentService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    // ĐÃ BỔ SUNG: API Tạo Question mới (Fix lỗi 404 Not Found)
    @PostMapping("/questions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Tạo câu hỏi mới trong 1 Lesson")
    public ResponseEntity<QuestionResponse> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminContentService.createQuestion(request));
    }

    // ================= USERS/ADMIN =================

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Lấy danh sách tất cả user")
    public ResponseEntity<List<AdminSummaryResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Lấy chi tiết 1 user")
    public ResponseEntity<AdminSummaryResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Cập nhật thông tin user")
    public ResponseEntity<AdminSummaryResponse> updateUser(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Xoá user")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Operation(summary = "[Admin] Tạo tài khoản Admin mới")
    public ResponseEntity<AdminSummaryResponse> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        });

        String username = generateAdminUsername(request.getEmail());

        User admin = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName().trim())
                .username(username)
                .role("ADMIN")
                .build();

        User saved = userRepository.save(admin);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                AdminSummaryResponse.builder()
                        .id(saved.getId())
                        .email(saved.getEmail())
                        .username(saved.getUsername())
                        .displayName(saved.getDisplayName())
                        .role(saved.getRole())
                        .build()
        );
    }

    private String generateAdminUsername(String email) {
        String localPart = email.substring(0, Math.max(email.indexOf('@'), 0));
        String normalized = localPart.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        if (normalized.isBlank()) {
            normalized = "admin";
        }

        String candidate = "admin_" + normalized;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = "admin_" + normalized + suffix;
            suffix++;
        }
        return candidate;
    }
}