package com.studyroom.service;

import com.studyroom.dto.NotificationVO;
import com.studyroom.entity.Notification;
import com.studyroom.exception.BusinessException;
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
    public void sendNotification(Long userId, String title, String content, String type, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type != null ? type : "SYSTEM");
        notification.setRelatedId(relatedId);
        notification.setStatus("UNREAD");

        notificationRepository.save(notification);
    }

    @Transactional
    public void sendWaitlistSuccessNotification(Long userId, String studyRoomName, String date, String startTime, String endTime, Long reservationId) {
        String title = "候补预约成功通知";
        String content = String.format("恭喜！您候补的%s %s %s-%s时间段已预约成功，请按时前往。",
                studyRoomName, date, startTime, endTime);

        sendNotification(userId, title, content, "WAITLIST_SUCCESS", reservationId);
    }

    public List<NotificationVO> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    public List<NotificationVO> getUserUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "UNREAD");
        return notifications.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, "UNREAD");
    }

    @Transactional
    public void markAsRead(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("通知不存在"));

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此通知");
        }

        notification.setStatus("READ");
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "UNREAD");
        for (Notification notification : notifications) {
            notification.setStatus("READ");
            notificationRepository.save(notification);
        }
    }

    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setUserId(notification.getUserId());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setType(notification.getType());
        vo.setRelatedId(notification.getRelatedId());
        vo.setStatus(notification.getStatus());
        vo.setCreatedAt(notification.getCreatedAt());
        return vo;
    }
}
