package com.shopflow.auth.service;

import com.shopflow.auth.dto.request.LoginRequest;
import com.shopflow.auth.dto.request.RegisterRequest;
import com.shopflow.auth.dto.response.LoginResponse;
import com.shopflow.auth.dto.response.RegisterResponse;
import com.shopflow.auth.dto.response.UserProfileResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserProfileResponse getCurrentUser(String email);
}