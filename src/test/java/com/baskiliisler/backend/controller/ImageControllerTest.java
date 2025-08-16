package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.ImageUploadResponseDto;
import com.baskiliisler.backend.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController imageController;

    private MockMultipartFile validImageFile;
    private MockMultipartFile invalidFile;
    private ImageUploadResponseDto successResponse;

    @BeforeEach
    void setUp() {
        validImageFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        successResponse = new ImageUploadResponseDto(
                "test_image_123",
                "https://res.cloudinary.com/test/image/upload/test_image_123.jpg",
                "Image başarıyla yüklendi",
                true
        );
    }

    @Test
    void testUploadImage_Success() {
        when(imageService.uploadImage(any())).thenReturn(successResponse);

        ResponseEntity<ImageUploadResponseDto> response = imageController.uploadImage(validImageFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(successResponse, response.getBody());
        verify(imageService).uploadImage(validImageFile);
    }

    @Test
    void testUploadImage_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

        ResponseEntity<ImageUploadResponseDto> response = imageController.uploadImage(emptyFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Dosya boş olamaz", response.getBody().message());
        assertFalse(response.getBody().success());
    }

    @Test
    void testUploadImage_InvalidFileType() {
        ResponseEntity<ImageUploadResponseDto> response = imageController.uploadImage(invalidFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Sadece image dosyaları kabul edilir", response.getBody().message());
        assertFalse(response.getBody().success());
    }

    @Test
    void testUploadImage_FileTooLarge() {
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile largeFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", largeContent);

        ResponseEntity<ImageUploadResponseDto> response = imageController.uploadImage(largeFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Dosya boyutu 5MB'dan büyük olamaz", response.getBody().message());
        assertFalse(response.getBody().success());
    }

    @Test
    void testUploadImage_ServiceException() {
        when(imageService.uploadImage(any())).thenThrow(new RuntimeException("Upload failed"));

        ResponseEntity<ImageUploadResponseDto> response = imageController.uploadImage(validImageFile);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("Image yüklenirken hata oluştu"));
        assertFalse(response.getBody().success());
    }

    @Test
    void testDeleteImage_Success() {
        String imageId = "test_image_123";
        ImageUploadResponseDto deleteResponse = new ImageUploadResponseDto(
                imageId, null, "Image başarıyla silindi", true
        );
        when(imageService.deleteImage(imageId)).thenReturn(deleteResponse);

        ResponseEntity<ImageUploadResponseDto> response = imageController.deleteImage(imageId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(imageId, response.getBody().imageId());
        assertEquals("Image başarıyla silindi", response.getBody().message());
        assertTrue(response.getBody().success());
        verify(imageService).deleteImage(imageId);
    }

    @Test
    void testDeleteImage_ServiceException() {
        String imageId = "test_image_123";
        doThrow(new RuntimeException("Delete failed")).when(imageService).deleteImage(imageId);

        ResponseEntity<ImageUploadResponseDto> response = imageController.deleteImage(imageId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(imageId, response.getBody().imageId());
        assertTrue(response.getBody().message().contains("Image silinirken hata oluştu"));
        assertFalse(response.getBody().success());
    }
}
