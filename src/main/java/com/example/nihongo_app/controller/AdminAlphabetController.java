package com.example.nihongo_app.controller;

import com.example.nihongo_app.dto.request.CreateAlphabetRequest;
import com.example.nihongo_app.dto.response.AlphabetResponse;
import com.example.nihongo_app.service.AlphabetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/alphabets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Alphabet", description = "Quản trị dữ liệu bảng chữ cái")
public class AdminAlphabetController {

    private final AlphabetService alphabetService;

    @PostMapping
    @Operation(summary = "Thêm một chữ cái")
    public ResponseEntity<AlphabetResponse> create(@Valid @RequestBody CreateAlphabetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alphabetService.create(request));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Thêm nhiều chữ cái")
    public ResponseEntity<List<AlphabetResponse>> createBulk(
            @Valid @NotEmpty @RequestBody List<@Valid CreateAlphabetRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alphabetService.createBulk(requests));
    }
}