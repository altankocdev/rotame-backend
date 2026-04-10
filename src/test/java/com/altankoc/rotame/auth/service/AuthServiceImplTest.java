package com.altankoc.rotame.auth.service;

import com.altankoc.rotame.auth.dto.AuthResponse;
import com.altankoc.rotame.auth.dto.LoginRequest;
import com.altankoc.rotame.auth.dto.RegisterRequest;
import com.altankoc.rotame.core.exception.BusinessException;
import com.altankoc.rotame.core.security.JwtService;
import com.altankoc.rotame.user.entity.User;
import com.altankoc.rotame.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .firstName("Altan")
                .lastName("Koç")
                .username("altankoc")
                .email("altan@test.com")
                .password("encodedPassword")
                .build();

        registerRequest = RegisterRequest.builder()
                .firstName("Altan")
                .lastName("Koç")
                .username("altankoc")
                .email("altan@test.com")
                .password("12345678")
                .build();

        loginRequest = LoginRequest.builder()
                .identifier("altan@test.com")
                .password("12345678")
                .build();
    }

    // ==================== REGISTER ====================

    @Test
    @DisplayName("Register - Başarılı kayıt")
    void register_Success() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.username())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateAccessToken(any())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(registerRequest.email());
        assertThat(response.accessToken()).isEqualTo("accessToken");
        assertThat(response.refreshToken()).isEqualTo("refreshToken");
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    @DisplayName("Register - Email zaten kullanılıyor")
    void register_EmailAlreadyExists_ThrowsBusinessException() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Bu e-posta adresi zaten kullanılıyor!");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register - Username zaten kullanılıyor")
    void register_UsernameAlreadyExists_ThrowsBusinessException() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.username())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Bu kullanıcı adı zaten kullanılıyor!");

        verify(userRepository, never()).save(any());
    }

    // ==================== LOGIN ====================

    @Test
    @DisplayName("Login - Email ile başarılı giriş")
    void login_WithEmail_Success() {
        when(userRepository.findByEmail(loginRequest.identifier())).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(jwtService.generateAccessToken(any())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");
        when(userRepository.save(any())).thenReturn(testUser);

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(testUser.getEmail());
        assertThat(response.accessToken()).isEqualTo("accessToken");
    }

    @Test
    @DisplayName("Login - Username ile başarılı giriş")
    void login_WithUsername_Success() {
        LoginRequest usernameLogin = LoginRequest.builder()
                .identifier("altankoc")
                .password("12345678")
                .build();

        when(userRepository.findByUsername("altankoc")).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(jwtService.generateAccessToken(any())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");
        when(userRepository.save(any())).thenReturn(testUser);

        AuthResponse response = authService.login(usernameLogin);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("Login - Kullanıcı bulunamadı")
    void login_UserNotFound_ThrowsBusinessException() {
        when(userRepository.findByEmail(loginRequest.identifier())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Kullanıcı bulunamadı!");
    }

    @Test
    @DisplayName("Login - Yanlış şifre")
    void login_WrongPassword_ThrowsBadCredentialsException() {
        when(userRepository.findByEmail(loginRequest.identifier())).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ==================== REFRESH ====================

    @Test
    @DisplayName("Refresh - Başarılı token yenileme")
    void refresh_Success() {
        testUser.setRefreshToken("validRefreshToken");
        when(userRepository.findByRefreshToken("validRefreshToken")).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(true);
        when(jwtService.generateAccessToken(any())).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken(any())).thenReturn("newRefreshToken");
        when(userRepository.save(any())).thenReturn(testUser);

        AuthResponse response = authService.refresh("validRefreshToken");

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("newAccessToken");
        assertThat(response.refreshToken()).isEqualTo("newRefreshToken");
    }

    @Test
    @DisplayName("Refresh - Geçersiz token")
    void refresh_InvalidToken_ThrowsBusinessException() {
        when(userRepository.findByRefreshToken("invalidToken")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("invalidToken"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Geçersiz refresh token!");
    }

    @Test
    @DisplayName("Refresh - Süresi dolmuş token")
    void refresh_ExpiredToken_ThrowsBusinessException() {
        testUser.setRefreshToken("expiredToken");
        when(userRepository.findByRefreshToken("expiredToken")).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(testUser);

        assertThatThrownBy(() -> authService.refresh("expiredToken"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Refresh token süresi dolmuş, lütfen tekrar giriş yapın!");
    }

    // ==================== LOGOUT ====================

    @Test
    @DisplayName("Logout - Başarılı çıkış")
    void logout_Success() {
        testUser.setRefreshToken("validRefreshToken");
        when(userRepository.findByRefreshToken("validRefreshToken")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        authService.logout("validRefreshToken");

        assertThat(testUser.getRefreshToken()).isNull();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Logout - Geçersiz token")
    void logout_InvalidToken_ThrowsBusinessException() {
        when(userRepository.findByRefreshToken("invalidToken")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logout("invalidToken"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Geçersiz refresh token!");
    }
}