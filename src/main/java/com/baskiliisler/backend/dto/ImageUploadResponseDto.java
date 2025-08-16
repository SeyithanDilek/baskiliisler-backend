package com.baskiliisler.backend.dto;

public record ImageUploadResponseDto(
        String imageId,        // Cloudinary public ID
        String imageUrl,       // Cloudinary secure URL
        String message,        // Başarı mesajı
        boolean success        // İşlem durumu
) {}
