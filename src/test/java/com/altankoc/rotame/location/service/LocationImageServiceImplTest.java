package com.altankoc.rotame.location.service;

import com.altankoc.rotame.core.exception.BusinessException;
import com.altankoc.rotame.core.exception.ResourceNotFoundException;
import com.altankoc.rotame.core.exception.UnauthorizedException;
import com.altankoc.rotame.core.s3.S3Service;
import com.altankoc.rotame.location.dto.LocationImageResponse;
import com.altankoc.rotame.location.entity.Location;
import com.altankoc.rotame.location.entity.LocationImage;
import com.altankoc.rotame.location.mapper.LocationImageMapper;
import com.altankoc.rotame.location.repository.LocationImageRepository;
import com.altankoc.rotame.location.repository.LocationRepository;
import com.altankoc.rotame.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationImageServiceImpl Tests")
class LocationImageServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationImageRepository locationImageRepository;

    @Mock
    private LocationImageMapper locationImageMapper;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private LocationImageServiceImpl locationImageService;

    private User testUser;
    private Location testLocation;
    private LocationImage testImage;
    private LocationImageResponse testImageResponse;
    private MockMultipartFile validFile;

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

        testLocation = Location.builder()
                .name("Zonguldak Sahili")
                .latitude(41.4564)
                .longitude(31.7987)
                .user(testUser)
                .build();
        setId(testLocation, 1L);

        testImage = LocationImage.builder()
                .imageUrl("https://rotame-images.s3.amazonaws.com/locations/1/uuid-test.jpg")
                .cover(true)
                .displayOrder(1)
                .location(testLocation)
                .build();
        setId(testImage, 1L);

        testImageResponse = LocationImageResponse.builder()
                .id(1L)
                .imageUrl("https://rotame-images.s3.amazonaws.com/locations/1/uuid-test.jpg")
                .cover(true)
                .displayOrder(1)
                .build();

        validFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    // ==================== UPLOAD ====================

    @Test
    @DisplayName("Upload - Başarılı resim yükleme")
    void upload_Success() throws Exception {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationImageRepository.countByLocationIdAndDeletedFalse(1L)).thenReturn(0);
        when(s3Service.uploadFile(anyString(), any(), anyString()))
                .thenReturn("https://rotame-images.s3.amazonaws.com/locations/1/uuid-test.jpg");
        when(locationImageRepository.save(any())).thenReturn(testImage);
        when(locationImageMapper.toResponse(any())).thenReturn(testImageResponse);

        LocationImageResponse response = locationImageService.upload(1L, 1L, validFile);

        assertThat(response).isNotNull();
        assertThat(response.imageUrl()).contains("locations/1");
        verify(s3Service).uploadFile(anyString(), any(), anyString());
        verify(locationImageRepository).save(any());
    }

    @Test
    @DisplayName("Upload - İlk resim kapak olur")
    void upload_FirstImage_IsCover() throws Exception {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationImageRepository.countByLocationIdAndDeletedFalse(1L)).thenReturn(0);
        when(s3Service.uploadFile(anyString(), any(), anyString())).thenReturn("https://url");
        when(locationImageMapper.toResponse(any())).thenReturn(testImageResponse);

        locationImageService.upload(1L, 1L, validFile);

        verify(locationImageRepository).save(argThat(img -> img.isCover()));
    }

    @Test
    @DisplayName("Upload - Maksimum resim sayısı aşıldı")
    void upload_MaxImagesExceeded_ThrowsBusinessException() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationImageRepository.countByLocationIdAndDeletedFalse(1L)).thenReturn(4);

        assertThatThrownBy(() -> locationImageService.upload(1L, 1L, validFile))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Bir konuma en fazla 4 resim eklenebilir!");
    }

    @Test
    @DisplayName("Upload - Geçersiz dosya tipi")
    void upload_InvalidContentType_ThrowsBusinessException() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "pdf content".getBytes()
        );

        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationImageRepository.countByLocationIdAndDeletedFalse(1L)).thenReturn(0);

        assertThatThrownBy(() -> locationImageService.upload(1L, 1L, invalidFile))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Sadece JPEG, PNG ve WebP formatları desteklenmektedir!");
    }

    @Test
    @DisplayName("Upload - Dosya boyutu aşıldı")
    void upload_FileSizeExceeded_ThrowsBusinessException() {
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.jpg", "image/jpeg", new byte[6 * 1024 * 1024]
        );

        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationImageRepository.countByLocationIdAndDeletedFalse(1L)).thenReturn(0);

        assertThatThrownBy(() -> locationImageService.upload(1L, 1L, largeFile))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Dosya boyutu 5MB'dan büyük olamaz!");
    }

    @Test
    @DisplayName("Upload - Konum bulunamadı")
    void upload_LocationNotFound_ThrowsResourceNotFoundException() {
        when(locationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationImageService.upload(99L, 1L, validFile))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Konum bulunamadı!");
    }

    @Test
    @DisplayName("Upload - Başka kullanıcının konumu")
    void upload_OtherUsersLocation_ThrowsUnauthorizedException() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));

        assertThatThrownBy(() -> locationImageService.upload(1L, 2L, validFile))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Bu konuma erişim yetkiniz yok!");
    }

    // ==================== GET ALL ====================

    @Test
    @DisplayName("GetAll - Başarılı resim listeleme")
    void getAll_Success() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationImageRepository.findByLocationIdAndDeletedFalse(1L))
                .thenReturn(List.of(testImage));
        when(locationImageMapper.toResponse(testImage)).thenReturn(testImageResponse);

        List<LocationImageResponse> response = locationImageService.getAll(1L, 1L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).imageUrl()).contains("locations/1");
    }

    @Test
    @DisplayName("GetAll - Başka kullanıcının konumu")
    void getAll_OtherUsersLocation_ThrowsUnauthorizedException() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));

        assertThatThrownBy(() -> locationImageService.getAll(1L, 2L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Bu konuma erişim yetkiniz yok!");
    }

    // ==================== DELETE ====================

    @Test
    @DisplayName("Delete - Başarılı resim silme")
    void delete_Success() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationImageRepository.findById(1L)).thenReturn(Optional.of(testImage));

        locationImageService.delete(1L, 1L, 1L);

        assertThat(testImage.isDeleted()).isTrue();
        verify(s3Service).deleteFile(anyString());
        verify(locationImageRepository).save(testImage);
    }

    @Test
    @DisplayName("Delete - Resim bulunamadı")
    void delete_ImageNotFound_ThrowsResourceNotFoundException() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationImageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationImageService.delete(1L, 99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Resim bulunamadı!");
    }

    @Test
    @DisplayName("Delete - Başka kullanıcının konumu")
    void delete_OtherUsersLocation_ThrowsUnauthorizedException() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));

        assertThatThrownBy(() -> locationImageService.delete(1L, 1L, 2L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Bu konuma erişim yetkiniz yok!");
    }

    // ==================== SET COVER ====================

    @Test
    @DisplayName("SetCover - Başarılı kapak resmi ayarlama")
    void setCover_Success() {
        LocationImage otherImage = LocationImage.builder()
                .imageUrl("https://url/other.jpg")
                .cover(true)
                .location(testLocation)
                .build();
        setId(otherImage, 2L);

        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationImageRepository.findByLocationIdAndDeletedFalse(1L))
                .thenReturn(List.of(otherImage));
        when(locationImageRepository.findById(1L)).thenReturn(Optional.of(testImage));
        when(locationImageRepository.save(any())).thenReturn(testImage);
        when(locationImageMapper.toResponse(testImage)).thenReturn(testImageResponse);

        locationImageService.setCover(1L, 1L, 1L);

        assertThat(otherImage.isCover()).isFalse();
        assertThat(testImage.isCover()).isTrue();
    }

    @Test
    @DisplayName("SetCover - Resim bulunamadı")
    void setCover_ImageNotFound_ThrowsResourceNotFoundException() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationImageRepository.findByLocationIdAndDeletedFalse(1L)).thenReturn(List.of());
        when(locationImageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationImageService.setCover(1L, 99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Resim bulunamadı!");
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