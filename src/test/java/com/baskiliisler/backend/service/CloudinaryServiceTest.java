package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.ImageUploadResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Test
    void testImageUploadResponseDto() {
        ImageUploadResponseDto response = new ImageUploadResponseDto(
                "test_id",
                "https://test.com/image.jpg",
                "Success",
                true
        );

        assertEquals("test_id", response.imageId());
        assertEquals("https://test.com/image.jpg", response.imageUrl());
        assertEquals("Success", response.message());
        assertTrue(response.success());
    }

    @Test
    void testMockMultipartFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        assertEquals("test.jpg", file.getOriginalFilename());
        assertEquals("image/jpeg", file.getContentType());
        assertFalse(file.isEmpty());
    }
}
