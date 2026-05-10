package com.studyroom.repository;

import com.studyroom.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = ?1 AND n.read = false")
    long countUnreadByUserId(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = ?1 AND n.read = false")
    void markAllReadByUserId(Long userId);
}
