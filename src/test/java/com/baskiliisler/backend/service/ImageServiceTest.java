package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.ImageUploadResponseDto;
import com.baskiliisler.backend.model.Image;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.repository.ImageRepository;
import com.baskiliisler.backend.service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ImageService imageService;

    private MockMultipartFile testFile;
    private User testUser;
    private ImageUploadResponseDto cloudinaryResponse;
    private Image testImage;

    @BeforeEach
    void setUp() {
        testFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        cloudinaryResponse = new ImageUploadResponseDto(
                "test_image_123",
                "https://res.cloudinary.com/test/image/upload/test_image_123.jpg",
                "Image başarıyla yüklendi",
                true
        );

        testImage = Image.builder()
                .id(1L)
                .cloudinaryId("test_image_123")
                .cloudinaryUrl("https://res.cloudinary.com/test/image/upload/test_image_123.jpg")
                .originalFilename("test.jpg")
                .fileSize(12L)
                .contentType("image/jpeg")
                .uploadedBy(testUser)
                .build();
    }

    @Test
    void testUploadImage_Success() {
        when(cloudinaryService.uploadGeneralImage(any())).thenReturn(cloudinaryResponse);
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(imageRepository.save(any(Image.class))).thenReturn(testImage);

        ImageUploadResponseDto result = imageService.uploadImage(testFile);

        assertNotNull(result);
        assertEquals("test_image_123", result.imageId());
        assertEquals("https://res.cloudinary.com/test/image/upload/test_image_123.jpg", result.imageUrl());
        assertEquals("Image başarıyla yüklendi ve kaydedildi", result.message());
        assertTrue(result.success());

        verify(cloudinaryService).uploadGeneralImage(testFile);
        verify(userService).getCurrentUser();
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void testUploadImage_CloudinaryError() {
        when(cloudinaryService.uploadGeneralImage(any())).thenThrow(new RuntimeException("Cloudinary error"));

        assertThrows(RuntimeException.class, () -> imageService.uploadImage(testFile));

        verify(cloudinaryService).uploadGeneralImage(testFile);
        verify(userService, never()).getCurrentUser();
        verify(imageRepository, never()).save(any());
    }

    @Test
    void testDeleteImage_Success() {
        doNothing().when(cloudinaryService).deleteImage("test_image_123");
        doNothing().when(imageRepository).softDeleteByCloudinaryId("test_image_123");

        ImageUploadResponseDto result = imageService.deleteImage("test_image_123");

        assertNotNull(result);
        assertEquals("test_image_123", result.imageId());
        assertEquals("Image başarıyla silindi", result.message());
        assertTrue(result.success());

        verify(cloudinaryService).deleteImage("test_image_123");
        verify(imageRepository).softDeleteByCloudinaryId("test_image_123");
    }

    @Test
    void testDeleteImage_CloudinaryError() {
        doThrow(new RuntimeException("Cloudinary error")).when(cloudinaryService).deleteImage("test_image_123");

        assertThrows(RuntimeException.class, () -> imageService.deleteImage("test_image_123"));

        verify(cloudinaryService).deleteImage("test_image_123");
        verify(imageRepository, never()).softDeleteByCloudinaryId(any());
    }

    @Test
    void testGetAllActiveImages() {
        List<Image> expectedImages = Arrays.asList(testImage);
        when(imageRepository.findAllActiveImages()).thenReturn(expectedImages);

        List<Image> result = imageService.getAllActiveImages();

        assertEquals(expectedImages, result);
        verify(imageRepository).findAllActiveImages();
    }

    @Test
    void testGetImagesByCurrentUser() {
        List<Image> expectedImages = Arrays.asList(testImage);
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(imageRepository.findByUploadedByIdAndIsDeletedFalse(1L)).thenReturn(expectedImages);

        List<Image> result = imageService.getImagesByCurrentUser();

        assertEquals(expectedImages, result);
        verify(userService).getCurrentUser();
        verify(imageRepository).findByUploadedByIdAndIsDeletedFalse(1L);
    }

    @Test
    void testGetImageByCloudinaryId_Success() {
        when(imageRepository.findActiveByCloudinaryId("test_image_123")).thenReturn(Optional.of(testImage));

        Image result = imageService.getImageByCloudinaryId("test_image_123");

        assertEquals(testImage, result);
        verify(imageRepository).findActiveByCloudinaryId("test_image_123");
    }

    @Test
    void testGetImageByCloudinaryId_NotFound() {
        when(imageRepository.findActiveByCloudinaryId("non_existent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> imageService.getImageByCloudinaryId("non_existent"));

        verify(imageRepository).findActiveByCloudinaryId("non_existent");
    }
}
