package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.dto.NotificationVO;
import com.studyroom.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public Result<List<NotificationVO>> getUserNotifications(@PathVariable Long userId) {
        List<NotificationVO> notifications = notificationService.getUserNotifications(userId);
        return Result.success(notifications);
    }

    @GetMapping("/user/{userId}/unread")
    public Result<List<NotificationVO>> getUserUnreadNotifications(@PathVariable Long userId) {
        List<NotificationVO> notifications = notificationService.getUserUnreadNotifications(userId);
        return Result.success(notifications);
    }

    @GetMapping("/user/{userId}/unread-count")
    public Result<Long> getUnreadCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PutMapping("/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id, @RequestParam Long userId) {
        notificationService.markAsRead(id, userId);
        return Result.success("标记已读成功", null);
    }

    @PutMapping("/read-all")
    public Result<?> markAllAsRead(@RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success("全部标记已读成功", null);
    }
}
