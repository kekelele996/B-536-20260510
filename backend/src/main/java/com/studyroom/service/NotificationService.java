package com.studyroom.service;

import com.studyroom.dto.NotificationVO;
import com.studyroom.entity.Notification;
import com.studyroom.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification createNotification(Long userId, String title, String content, Long relatedReservationId, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedReservationId(relatedReservationId);
        notification.setType(type);
        notification.setStatus("UNREAD");
        return notificationRepository.save(notification);
    }

    public List<NotificationVO> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    public List<NotificationVO> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "UNREAD");
        return notifications.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, "UNREAD");
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setStatus("READ");
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "UNREAD");
        for (Notification notification : notifications) {
            notification.setStatus("READ");
        }
        notificationRepository.saveAll(notifications);
    }

    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setUserId(notification.getUserId());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setRelatedReservationId(notification.getRelatedReservationId());
        vo.setType(notification.getType());
        vo.setStatus(notification.getStatus());
        vo.setCreatedAt(notification.getCreatedAt());
        return vo;
    }
}
