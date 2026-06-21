package com.example.nihongo_app.service;

import com.example.nihongo_app.dto.request.LoginRequest;
import com.example.nihongo_app.dto.request.RegisterRequest;
import com.example.nihongo_app.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}