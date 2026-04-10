package com.altankoc.rotame.location.service;

import com.altankoc.rotame.core.exception.BusinessException;
import com.altankoc.rotame.core.exception.ResourceNotFoundException;
import com.altankoc.rotame.core.exception.UnauthorizedException;
import com.altankoc.rotame.location.dto.CreateLocationRequest;
import com.altankoc.rotame.location.dto.LocationResponse;
import com.altankoc.rotame.location.dto.UpdateLocationRequest;
import com.altankoc.rotame.location.entity.Location;
import com.altankoc.rotame.location.mapper.LocationMapper;
import com.altankoc.rotame.location.repository.LocationRepository;
import com.altankoc.rotame.user.entity.User;
import com.altankoc.rotame.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationServiceImpl Tests")
class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationServiceImpl locationService;

    private User testUser;
    private User otherUser;
    private Location testLocation;
    private LocationResponse testLocationResponse;
    private CreateLocationRequest createRequest;
    private UpdateLocationRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .firstName("Altan")
                .lastName("Koç")
                .username("altankoc")
                .email("altan@test.com")
                .password("encodedPassword")
                .build();
        // Reflection ile id set ediyoruz çünkü BaseEntity'de private
        setId(testUser, 1L);

        otherUser = User.builder()
                .firstName("Başka")
                .lastName("Kullanıcı")
                .username("baskakul")
                .email("baska@test.com")
                .password("encodedPassword")
                .build();
        setId(otherUser, 2L);

        testLocation = Location.builder()
                .name("Zonguldak Sahili")
                .description("Güzel bir sahil")
                .latitude(41.4564)
                .longitude(31.7987)
                .user(testUser)
                .build();
        setId(testLocation, 1L);

        testLocationResponse = LocationResponse.builder()
                .id(1L)
                .name("Zonguldak Sahili")
                .description("Güzel bir sahil")
                .latitude(41.4564)
                .longitude(31.7987)
                .favorite(false)
                .build();

        createRequest = CreateLocationRequest.builder()
                .name("Zonguldak Sahili")
                .description("Güzel bir sahil")
                .latitude(41.4564)
                .longitude(31.7987)
                .build();

        updateRequest = UpdateLocationRequest.builder()
                .name("Güncellenmiş Konum")
                .build();
    }

    // ==================== CREATE ====================

    @Test
    @DisplayName("Create - Başarılı konum oluşturma")
    void create_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(locationMapper.toEntity(createRequest)).thenReturn(testLocation);
        when(locationRepository.save(any())).thenReturn(testLocation);
        when(locationMapper.toResponse(testLocation)).thenReturn(testLocationResponse);

        LocationResponse response = locationService.create(createRequest, 1L);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Zonguldak Sahili");
        verify(locationRepository).save(testLocation);
    }

    @Test
    @DisplayName("Create - Kullanıcı bulunamadı")
    void create_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.create(createRequest, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Kullanıcı bulunamadı!");

        verify(locationRepository, never()).save(any());
    }

    // ==================== GET ALL ====================

    @Test
    @DisplayName("GetAll - Tüm konumları getir")
    void getAll_Success() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Location> locationPage = new PageImpl<>(List.of(testLocation));

        when(locationRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(1L, pageable))
                .thenReturn(locationPage);
        when(locationMapper.toResponse(testLocation)).thenReturn(testLocationResponse);

        Page<LocationResponse> response = locationService.getAll(1L, false, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).name()).isEqualTo("Zonguldak Sahili");
    }

    @Test
    @DisplayName("GetAll - Sadece favorileri getir")
    void getAll_OnlyFavorites_Success() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Location> locationPage = new PageImpl<>(List.of(testLocation));

        when(locationRepository.findByUserIdAndFavoriteTrueAndDeletedFalseOrderByCreatedAtDesc(1L, pageable))
                .thenReturn(locationPage);
        when(locationMapper.toResponse(testLocation)).thenReturn(testLocationResponse);

        Page<LocationResponse> response = locationService.getAll(1L, true, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
    }

    // ==================== GET BY ID ====================

    @Test
    @DisplayName("GetById - Başarılı konum getirme")
    void getById_Success() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationMapper.toResponse(testLocation)).thenReturn(testLocationResponse);

        LocationResponse response = locationService.getById(1L, 1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GetById - Konum bulunamadı")
    void getById_NotFound_ThrowsResourceNotFoundException() {
        when(locationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.getById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Konum bulunamadı!");
    }

    @Test
    @DisplayName("GetById - Başka kullanıcının konumu")
    void getById_OtherUsersLocation_ThrowsUnauthorizedException() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));

        assertThatThrownBy(() -> locationService.getById(1L, 2L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Bu konuma erişim yetkiniz yok!");
    }

    // ==================== UPDATE ====================

    @Test
    @DisplayName("Update - Başarılı güncelleme")
    void update_Success() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationRepository.save(any())).thenReturn(testLocation);
        when(locationMapper.toResponse(testLocation)).thenReturn(testLocationResponse);

        LocationResponse response = locationService.update(1L, updateRequest, 1L);

        assertThat(response).isNotNull();
        verify(locationMapper).updateEntity(updateRequest, testLocation);
        verify(locationRepository).save(testLocation);
    }

    @Test
    @DisplayName("Update - Başka kullanıcının konumu")
    void update_OtherUsersLocation_ThrowsUnauthorizedException() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));

        assertThatThrownBy(() -> locationService.update(1L, updateRequest, 2L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Bu konuma erişim yetkiniz yok!");
    }

    // ==================== DELETE ====================

    @Test
    @DisplayName("Delete - Başarılı soft delete")
    void delete_Success() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationRepository.save(any())).thenReturn(testLocation);

        locationService.delete(1L, 1L);

        assertThat(testLocation.isDeleted()).isTrue();
        verify(locationRepository).save(testLocation);
    }

    @Test
    @DisplayName("Delete - Başka kullanıcının konumu")
    void delete_OtherUsersLocation_ThrowsUnauthorizedException() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));

        assertThatThrownBy(() -> locationService.delete(1L, 2L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Bu konuma erişim yetkiniz yok!");
    }

    // ==================== TOGGLE FAVORITE ====================

    @Test
    @DisplayName("ToggleFavorite - Favoriye ekle")
    void toggleFavorite_AddToFavorites_Success() {
        assertThat(testLocation.isFavorite()).isFalse();
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationRepository.save(any())).thenReturn(testLocation);
        when(locationMapper.toResponse(testLocation)).thenReturn(testLocationResponse);

        locationService.toggleFavorite(1L, 1L);

        assertThat(testLocation.isFavorite()).isTrue();
        verify(locationRepository).save(testLocation);
    }

    @Test
    @DisplayName("ToggleFavorite - Favoriden çıkar")
    void toggleFavorite_RemoveFromFavorites_Success() {
        testLocation.setFavorite(true);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationRepository.save(any())).thenReturn(testLocation);
        when(locationMapper.toResponse(testLocation)).thenReturn(testLocationResponse);

        locationService.toggleFavorite(1L, 1L);

        assertThat(testLocation.isFavorite()).isFalse();
    }

    // ==================== RESTORE ====================

    @Test
    @DisplayName("Restore - Başarılı geri yükleme")
    void restore_Success() {
        testLocation.softDelete();
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationRepository.save(any())).thenReturn(testLocation);
        when(locationMapper.toResponse(testLocation)).thenReturn(testLocationResponse);

        locationService.restore(1L, 1L);

        assertThat(testLocation.isDeleted()).isFalse();
        verify(locationRepository).save(testLocation);
    }

    @Test
    @DisplayName("Restore - Konum zaten aktif")
    void restore_AlreadyActive_ThrowsBusinessException() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));

        assertThatThrownBy(() -> locationService.restore(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Konum zaten aktif!");
    }

    @Test
    @DisplayName("Restore - Başka kullanıcının konumu")
    void restore_OtherUsersLocation_ThrowsUnauthorizedException() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));

        assertThatThrownBy(() -> locationService.restore(1L, 2L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Bu konuma erişim yetkiniz yok!");
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