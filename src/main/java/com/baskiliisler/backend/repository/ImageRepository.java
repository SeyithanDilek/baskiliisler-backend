package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    
    Optional<Image> findByCloudinaryId(String cloudinaryId);
    
    List<Image> findByUploadedByIdAndIsDeletedFalse(Long uploadedById);
    
    @Query("SELECT i FROM Image i WHERE i.isDeleted = false ORDER BY i.createdAt DESC")
    List<Image> findAllActiveImages();
    
    @Modifying
    @Query("UPDATE Image i SET i.isDeleted = true WHERE i.cloudinaryId = :cloudinaryId")
    void softDeleteByCloudinaryId(@Param("cloudinaryId") String cloudinaryId);
    
    @Query("SELECT i FROM Image i WHERE i.cloudinaryId = :cloudinaryId AND i.isDeleted = false")
    Optional<Image> findActiveByCloudinaryId(@Param("cloudinaryId") String cloudinaryId);
}
