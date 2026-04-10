package com.altankoc.rotame.user.service;

import com.altankoc.rotame.core.exception.BusinessException;
import com.altankoc.rotame.core.exception.ResourceNotFoundException;
import com.altankoc.rotame.user.dto.UpdateUserRequest;
import com.altankoc.rotame.user.dto.UserResponse;
import com.altankoc.rotame.user.entity.User;
import com.altankoc.rotame.user.mapper.UserMapper;
import com.altankoc.rotame.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserResponse testUserResponse;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .firstName("Altan")
                .lastName("Koç")
                .username("altankoc")
                .email("altan@test.com")
                .password("encodedPassword")
                .build();

        setId(testUser, 1L);

        testUserResponse = UserResponse.builder()
                .id(1L)
                .firstName("Altan")
                .lastName("Koç")
                .username("altankoc")
                .email("altan@test.com")
                .role("USER")
                .authProvider("LOCAL")
                .build();

        updateRequest = UpdateUserRequest.builder()
                .firstName("Yeni Ad")
                .lastName("Yeni Soyad")
                .username("yeniusername")
                .build();
    }

    // ==================== GET ME ====================

    @Test
    @DisplayName("GetMe - Başarılı profil getirme")
    void getMe_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        UserResponse response = userService.getMe(1L);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("altan@test.com");
        assertThat(response.username()).isEqualTo("altankoc");
    }

    @Test
    @DisplayName("GetMe - Kullanıcı bulunamadı")
    void getMe_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Kullanıcı bulunamadı!");
    }

    @Test
    @DisplayName("GetMe - Silinmiş kullanıcı")
    void getMe_DeletedUser_ThrowsResourceNotFoundException() {
        testUser.softDelete();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.getMe(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Kullanıcı bulunamadı!");
    }

    // ==================== UPDATE ME ====================

    @Test
    @DisplayName("UpdateMe - Başarılı güncelleme")
    void updateMe_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("yeniusername")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        UserResponse response = userService.updateMe(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(userMapper).updateEntity(updateRequest, testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("UpdateMe - Username zaten kullanılıyor")
    void updateMe_UsernameAlreadyExists_ThrowsBusinessException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("yeniusername")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateMe(1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Bu kullanıcı adı zaten kullanılıyor!");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("UpdateMe - Aynı username ile güncelleme")
    void updateMe_SameUsername_Success() {
        UpdateUserRequest sameUsernameRequest = UpdateUserRequest.builder()
                .firstName("Yeni Ad")
                .username("altankoc") // aynı username
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        UserResponse response = userService.updateMe(1L, sameUsernameRequest);

        assertThat(response).isNotNull();
        // Aynı username olduğu için existsByUsername çağrılmamalı
        verify(userRepository, never()).existsByUsername(any());
    }

    @Test
    @DisplayName("UpdateMe - Kullanıcı bulunamadı")
    void updateMe_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateMe(99L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Kullanıcı bulunamadı!");
    }

    // ==================== DELETE ME ====================

    @Test
    @DisplayName("DeleteMe - Başarılı soft delete")
    void deleteMe_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        userService.deleteMe(1L);

        assertThat(testUser.isDeleted()).isTrue();
        assertThat(testUser.getRefreshToken()).isNull();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("DeleteMe - Kullanıcı bulunamadı")
    void deleteMe_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteMe(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Kullanıcı bulunamadı!");
    }

    @Test
    @DisplayName("DeleteMe - Refresh token temizlendi")
    void deleteMe_RefreshTokenCleared() {
        testUser.setRefreshToken("someRefreshToken");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        userService.deleteMe(1L);

        assertThat(testUser.getRefreshToken()).isNull();
    }

    // ==================== HELPER ====================

    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}