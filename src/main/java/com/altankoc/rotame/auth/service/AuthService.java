package com.altankoc.rotame.auth.service;

import com.altankoc.rotame.auth.dto.AuthResponse;
import com.altankoc.rotame.auth.dto.LoginRequest;
import com.altankoc.rotame.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String refreshToken);
    void logout(String refreshToken);
}