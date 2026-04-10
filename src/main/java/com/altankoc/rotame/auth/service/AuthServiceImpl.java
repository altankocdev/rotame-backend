package com.altankoc.rotame.auth.service;

import com.altankoc.rotame.auth.dto.AuthResponse;
import com.altankoc.rotame.auth.dto.LoginRequest;
import com.altankoc.rotame.auth.dto.RegisterRequest;
import com.altankoc.rotame.core.exception.BusinessException;
import com.altankoc.rotame.core.security.CustomUserDetails;
import com.altankoc.rotame.core.security.JwtService;
import com.altankoc.rotame.user.entity.User;
import com.altankoc.rotame.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Bu e-posta adresi zaten kullanılıyor!");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Bu kullanıcı adı zaten kullanılıyor!");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {

        User user = request.identifier().contains("@")
                ? userRepository.findByEmail(request.identifier())
                .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı!"))
                : userRepository.findByUsername(request.identifier())
                .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı!"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        request.password()
                )
        );

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        log.info("User logged in: {}", user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken) {

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException("Geçersiz refresh token!"));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            user.setRefreshToken(null);
            userRepository.save(user);
            throw new BusinessException("Refresh token süresi dolmuş, lütfen tekrar giriş yapın!");
        }

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        log.info("Tokens refreshed for user: {}", user.getEmail());

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException("Geçersiz refresh token!"));

        user.setRefreshToken(null);
        userRepository.save(user);

        log.info("User logged out: {}", user.getEmail());
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}