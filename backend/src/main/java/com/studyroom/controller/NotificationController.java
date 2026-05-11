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
        List<NotificationVO> list = notificationService.getUserNotifications(userId);
        return Result.success(list);
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@RequestParam Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PutMapping("/{id}/read")
    public Result<?> markRead(@PathVariable Long id, @RequestParam Long userId) {
        notificationService.markRead(id, userId);
        return Result.success("已标记为已读", null);
    }

    @PutMapping("/read-all")
    public Result<?> markAllRead(@RequestParam Long userId) {
        notificationService.markAllRead(userId);
        return Result.success("全部标记为已读", null);
    }
}
