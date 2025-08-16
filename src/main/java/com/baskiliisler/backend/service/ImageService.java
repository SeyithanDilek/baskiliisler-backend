package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.ImageUploadResponseDto;
import com.baskiliisler.backend.model.Image;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.repository.ImageRepository;
import com.baskiliisler.backend.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
    
    private final CloudinaryService cloudinaryService;
    private final ImageRepository imageRepository;
    private final UserService userService;
    
    @Transactional
    public ImageUploadResponseDto uploadImage(MultipartFile file) {
        try {
            // Cloudinary'ye yükle
            ImageUploadResponseDto cloudinaryResponse = cloudinaryService.uploadGeneralImage(file);
            
            // Mevcut kullanıcıyı al
            User currentUser = userService.getCurrentUser();
            
            // DB'ye kaydet
            Image image = Image.builder()
                    .cloudinaryId(cloudinaryResponse.imageId())
                    .cloudinaryUrl(cloudinaryResponse.imageUrl())
                    .originalFilename(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .uploadedBy(currentUser)
                    .build();
            
            Image savedImage = imageRepository.save(image);
            
            log.info("Image saved to database: ID={}, CloudinaryID={}", 
                    savedImage.getId(), savedImage.getCloudinaryId());
            
            return new ImageUploadResponseDto(
                    cloudinaryResponse.imageId(),
                    cloudinaryResponse.imageUrl(),
                    "Image başarıyla yüklendi ve kaydedildi",
                    true
            );
            
        } catch (Exception e) {
            log.error("Error uploading image: {}", e.getMessage(), e);
            throw new RuntimeException("Image upload failed", e);
        }
    }
    
    @Transactional
    public ImageUploadResponseDto deleteImage(String cloudinaryId) {
        try {
            // Cloudinary'den sil
            cloudinaryService.deleteImage(cloudinaryId);
            
            // DB'de soft delete yap
            imageRepository.softDeleteByCloudinaryId(cloudinaryId);
            
            log.info("Image soft deleted: CloudinaryID={}", cloudinaryId);
            
            return new ImageUploadResponseDto(
                    cloudinaryId,
                    null,
                    "Image başarıyla silindi",
                    true
            );
            
        } catch (Exception e) {
            log.error("Error deleting image: {}", e.getMessage(), e);
            throw new RuntimeException("Image deletion failed", e);
        }
    }
    
    public List<Image> getAllActiveImages() {
        return imageRepository.findAllActiveImages();
    }
    
    public List<Image> getImagesByCurrentUser() {
        User currentUser = userService.getCurrentUser();
        return imageRepository.findByUploadedByIdAndIsDeletedFalse(currentUser.getId());
    }
    
    public Image getImageByCloudinaryId(String cloudinaryId) {
        return imageRepository.findActiveByCloudinaryId(cloudinaryId)
                .orElseThrow(() -> new RuntimeException("Image not found: " + cloudinaryId));
    }
}
