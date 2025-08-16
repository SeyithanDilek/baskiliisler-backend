package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.ImageUploadResponseDto;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "baskili-isler/logos",
                            "public_id", "logo_" + System.currentTimeMillis(),
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );
            
            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("Image uploaded successfully: {}", imageUrl);
            return imageUrl;
            
        } catch (IOException e) {
            log.error("Error uploading image to Cloudinary", e);
            throw new RuntimeException("Image upload failed", e);
        }
    }

    public ImageUploadResponseDto uploadGeneralImage(MultipartFile file) {
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "baskili-isler/images",
                            "public_id", "img_" + System.currentTimeMillis(),
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );
            
            String imageId = (String) uploadResult.get("public_id");
            String imageUrl = (String) uploadResult.get("secure_url");
            
            log.info("General image uploaded successfully: ID={}, URL={}", imageId, imageUrl);
            
            return new ImageUploadResponseDto(
                    imageId,
                    imageUrl,
                    "Image başarıyla yüklendi",
                    true
            );
            
        } catch (IOException e) {
            log.error("Error uploading general image to Cloudinary", e);
            throw new RuntimeException("Image upload failed", e);
        }
    }

    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Image deleted successfully: {}", publicId);
        } catch (IOException e) {
            log.error("Error deleting image from Cloudinary", e);
            throw new RuntimeException("Image deletion failed", e);
        }
    }
} 