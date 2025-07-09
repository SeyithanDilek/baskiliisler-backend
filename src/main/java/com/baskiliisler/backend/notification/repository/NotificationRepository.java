package com.baskiliisler.backend.notification.repository;

import com.baskiliisler.backend.notification.entity.Notification;
import com.baskiliisler.backend.notification.type.NotificationPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Okunmamış bildirimler - Global
    List<Notification> findByIsReadFalseOrderByCreatedAtDesc();
    
    // Tüm bildirimler - Global
    List<Notification> findAllByOrderByCreatedAtDesc();
    
    // Okunmamış sayısı - Global
    long countByIsReadFalse();
    
    // Belirli bir tarihten sonraki bildirimler - Global
    List<Notification> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime after);
    
    // Önceliğe göre okunmamış bildirimler - Global
    List<Notification> findByIsReadFalseAndPriorityOrderByCreatedAtDesc(NotificationPriority priority);
    
    // Belirli önceliğe göre tüm okunmamış bildirimler
    @Query("SELECT n FROM Notification n WHERE n.isRead = false AND n.priority = :priority ORDER BY n.createdAt DESC")
    List<Notification> findAllUnreadByPriority(@Param("priority") NotificationPriority priority);
    
    // Belirli entity ile ilgili bildirimler
    List<Notification> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, Long entityId);
    
    // Eski bildirimleri temizleme
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoff")
    void deleteByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);
    
    // Tüm bildirimleri okunmuş olarak işaretle - Global
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.isRead = false")
    void markAllAsRead(@Param("readAt") LocalDateTime readAt);
} 