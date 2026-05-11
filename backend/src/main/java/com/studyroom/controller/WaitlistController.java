package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.dto.WaitlistRequest;
import com.studyroom.dto.WaitlistVO;
import com.studyroom.entity.Waitlist;
import com.studyroom.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistService waitlistService;

    @PostMapping
    public Result<Waitlist> joinWaitlist(@RequestBody WaitlistRequest request) {
        Waitlist waitlist = waitlistService.joinWaitlist(request);
        return Result.success("加入候补成功", waitlist);
    }

    @DeleteMapping("/{id}")
    public Result<?> cancelWaitlist(@PathVariable Long id, @RequestParam Long userId) {
        waitlistService.cancelWaitlist(id, userId);
        return Result.success("取消候补成功", null);
    }

    @GetMapping("/user/{userId}")
    public Result<List<WaitlistVO>> getUserWaitlist(@PathVariable Long userId) {
        List<WaitlistVO> waitlist = waitlistService.getUserWaitlist(userId);
        return Result.success(waitlist);
    }

    @GetMapping("/time-slot/{timeSlotId}")
    public Result<List<WaitlistVO>> getTimeSlotWaitlist(@PathVariable Long timeSlotId) {
        List<WaitlistVO> waitlist = waitlistService.getTimeSlotWaitlist(timeSlotId);
        return Result.success(waitlist);
    }

    @GetMapping("/check")
    public Result<Boolean> isUserInWaitlist(@RequestParam Long userId, @RequestParam Long timeSlotId) {
        boolean inWaitlist = waitlistService.isUserInWaitlist(userId, timeSlotId);
        return Result.success(inWaitlist);
    }

    @GetMapping("/count/{timeSlotId}")
    public Result<Long> getWaitlistCount(@PathVariable Long timeSlotId) {
        long count = waitlistService.getWaitlistCount(timeSlotId);
        return Result.success(count);
    }
}
