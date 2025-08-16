package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.ImageUploadResponseDto;
import com.baskiliisler.backend.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@Tag(name = "Image Management", description = "Image yönetimi API'leri")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    @Operation(summary = "Image yükle", description = "Genel image upload endpoint'i")
    public ResponseEntity<ImageUploadResponseDto> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ImageUploadResponseDto(null, null, "Dosya boş olamaz", false));
            }

            // Dosya tipi kontrolü
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(new ImageUploadResponseDto(null, null, "Sadece image dosyaları kabul edilir", false));
            }

            // Dosya boyutu kontrolü (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(new ImageUploadResponseDto(null, null, "Dosya boyutu 5MB'dan büyük olamaz", false));
            }

            ImageUploadResponseDto response = imageService.uploadImage(file);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImageUploadResponseDto(null, null, "Image yüklenirken hata oluştu: " + e.getMessage(), false));
        }
    }

    @DeleteMapping("/{imageId}")
    @Operation(summary = "Image sil", description = "Cloudinary'den image'ı siler")
    public ResponseEntity<ImageUploadResponseDto> deleteImage(@PathVariable String imageId) {
        try {
            imageService.deleteImage(imageId);
            return ResponseEntity.ok(new ImageUploadResponseDto(
                    imageId,
                    null,
                    "Image başarıyla silindi",
                    true
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImageUploadResponseDto(
                            imageId,
                            null,
                            "Image silinirken hata oluştu: " + e.getMessage(),
                            false
                    ));
        }
    }
}
