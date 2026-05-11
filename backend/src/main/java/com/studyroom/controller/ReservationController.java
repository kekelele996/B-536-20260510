package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.dto.ReservationRequest;
import com.studyroom.dto.ReservationVO;
import com.studyroom.entity.Reservation;
import com.studyroom.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public Result<Reservation> createReservation(@RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.createReservation(request);
        return Result.success("预约成功", reservation);
    }

    @PostMapping("/waitlist")
    public Result<Reservation> joinWaitlist(@RequestBody ReservationRequest request) {
        Reservation waitlist = reservationService.joinWaitlist(request);
        return Result.success("加入候补成功", waitlist);
    }

    @GetMapping("/user/{userId}")
    public Result<List<ReservationVO>> getUserReservations(@PathVariable Long userId) {
        List<ReservationVO> reservations = reservationService.getUserReservations(userId);
        return Result.success(reservations);
    }

    @GetMapping("/all")
    public Result<List<ReservationVO>> getAllReservations() {
        List<ReservationVO> reservations = reservationService.getAllReservations();
        return Result.success(reservations);
    }

    @GetMapping("/waitlist/count/{timeSlotId}")
    public Result<Long> getWaitlistCount(@PathVariable Long timeSlotId) {
        long count = reservationService.getWaitlistCount(timeSlotId);
        return Result.success(count);
    }

    @DeleteMapping("/{id}")
    public Result<?> cancelReservation(@PathVariable Long id, @RequestParam Long userId) {
        reservationService.cancelReservation(id, userId);
        return Result.success("取消成功", null);
    }

    @DeleteMapping("/waitlist/{id}")
    public Result<?> cancelWaitlist(@PathVariable Long id, @RequestParam Long userId) {
        reservationService.cancelWaitlist(id, userId);
        return Result.success("取消候补成功", null);
    }
}
