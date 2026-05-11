package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.dto.WaitlistVO;
import com.studyroom.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistService waitlistService;

    @PostMapping
    public Result<WaitlistVO> joinWaitlist(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        Long timeSlotId = body.get("timeSlotId");
        WaitlistVO vo = waitlistService.joinWaitlist(userId, timeSlotId);
        return Result.success("加入候补成功", vo);
    }

    @DeleteMapping("/{id}")
    public Result<?> leaveWaitlist(@PathVariable Long id, @RequestParam Long userId) {
        waitlistService.leaveWaitlist(id, userId);
        return Result.success("退出候补成功", null);
    }

    @GetMapping("/user/{userId}")
    public Result<List<WaitlistVO>> getUserWaitlist(@PathVariable Long userId) {
        List<WaitlistVO> list = waitlistService.getUserWaitlist(userId);
        return Result.success(list);
    }

    @GetMapping("/count")
    public Result<Long> getWaitlistCount(@RequestParam Long timeSlotId) {
        long count = waitlistService.getWaitlistCount(timeSlotId);
        return Result.success(count);
    }

    @GetMapping("/check")
    public Result<Boolean> isUserWaiting(@RequestParam Long userId, @RequestParam Long timeSlotId) {
        boolean waiting = waitlistService.isUserWaiting(userId, timeSlotId);
        return Result.success(waiting);
    }
}
