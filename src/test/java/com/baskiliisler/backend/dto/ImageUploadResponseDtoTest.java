package com.baskiliisler.backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ImageUploadResponseDtoTest {

    @Test
    void testImageUploadResponseDto() {
        String imageId = "test_image_123";
        String imageUrl = "https://res.cloudinary.com/test/image/upload/test_image_123.jpg";
        String message = "Image başarıyla yüklendi";
        boolean success = true;

        ImageUploadResponseDto response = new ImageUploadResponseDto(imageId, imageUrl, message, success);

        assertEquals(imageId, response.imageId());
        assertEquals(imageUrl, response.imageUrl());
        assertEquals(message, response.message());
        assertEquals(success, response.success());
    }

    @Test
    void testImageUploadResponseDtoWithNullValues() {
        ImageUploadResponseDto response = new ImageUploadResponseDto(null, null, "Hata", false);

        assertNull(response.imageId());
        assertNull(response.imageUrl());
        assertEquals("Hata", response.message());
        assertFalse(response.success());
    }
}
