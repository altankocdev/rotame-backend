package com.altankoc.rotame.user.service;

import com.altankoc.rotame.user.dto.UpdateUserRequest;
import com.altankoc.rotame.user.dto.UserResponse;

public interface UserService {
    UserResponse getMe(Long userId);
    UserResponse updateMe(Long userId, UpdateUserRequest request);
    void deleteMe(Long userId);
}