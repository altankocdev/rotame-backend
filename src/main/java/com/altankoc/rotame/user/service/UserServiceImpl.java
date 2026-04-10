package com.altankoc.rotame.user.service;

import com.altankoc.rotame.core.exception.BusinessException;
import com.altankoc.rotame.core.exception.ResourceNotFoundException;
import com.altankoc.rotame.user.dto.UpdateUserRequest;
import com.altankoc.rotame.user.dto.UserResponse;
import com.altankoc.rotame.user.entity.User;
import com.altankoc.rotame.user.mapper.UserMapper;
import com.altankoc.rotame.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        User user = getUser(userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateMe(Long userId, UpdateUserRequest request) {
        User user = getUser(userId);

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new BusinessException("Bu kullanıcı adı zaten kullanılıyor!");
            }
        }

        userMapper.updateEntity(request, user);
        userRepository.save(user);

        log.info("User updated: {}", userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void deleteMe(Long userId) {
        User user = getUser(userId);
        user.softDelete();
        user.setRefreshToken(null);
        userRepository.save(user);
        log.info("User deleted: {}", userId);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));
    }
}