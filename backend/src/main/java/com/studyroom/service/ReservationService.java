package com.studyroom.service;

import com.studyroom.dto.ReservationRequest;
import com.studyroom.dto.ReservationVO;
import com.studyroom.entity.Reservation;
import com.studyroom.entity.StudyRoom;
import com.studyroom.entity.TimeSlot;
import com.studyroom.entity.User;
import com.studyroom.exception.BusinessException;
import com.studyroom.repository.ReservationRepository;
import com.studyroom.repository.StudyRoomRepository;
import com.studyroom.repository.TimeSlotRepository;
import com.studyroom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final NotificationService notificationService;

    @Transactional
    public Reservation createReservation(ReservationRequest request) {
        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new BusinessException("时间段不存在"));

        long userActiveCount = reservationRepository.countActiveReservationsByUserAndTimeSlot(
                request.getUserId(), request.getTimeSlotId());
        if (userActiveCount > 0) {
            throw new BusinessException("您已经预约了该时间段，请勿重复预约");
        }

        long userWaitlistCount = reservationRepository.countWaitlistByUserAndTimeSlot(
                request.getUserId(), request.getTimeSlotId());
        if (userWaitlistCount > 0) {
            throw new BusinessException("您已在该时间段的候补队列中");
        }

        long reservedCount = reservationRepository.countActiveReservationsByTimeSlotId(request.getTimeSlotId());
        if (reservedCount >= timeSlot.getAvailableSeats()) {
            throw new BusinessException("该时间段已满，无法预约，请加入候补队列");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(request.getUserId());
        reservation.setTimeSlotId(request.getTimeSlotId());
        reservation.setSeatNumber((int) reservedCount + 1);
        reservation.setStatus("ACTIVE");

        Reservation saved = reservationRepository.save(reservation);

        timeSlot.setAvailableSeats(timeSlot.getAvailableSeats() - 1);
        timeSlotRepository.save(timeSlot);

        return saved;
    }

    @Transactional
    public Reservation joinWaitlist(ReservationRequest request) {
        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new BusinessException("时间段不存在"));

        long userActiveCount = reservationRepository.countActiveReservationsByUserAndTimeSlot(
                request.getUserId(), request.getTimeSlotId());
        if (userActiveCount > 0) {
            throw new BusinessException("您已经预约了该时间段");
        }

        long userWaitlistCount = reservationRepository.countWaitlistByUserAndTimeSlot(
                request.getUserId(), request.getTimeSlotId());
        if (userWaitlistCount > 0) {
            throw new BusinessException("您已在该时间段的候补队列中");
        }

        long reservedCount = reservationRepository.countActiveReservationsByTimeSlotId(request.getTimeSlotId());
        if (reservedCount < timeSlot.getAvailableSeats()) {
            throw new BusinessException("该时间段还有空位，请直接预约");
        }

        Reservation waitlist = new Reservation();
        waitlist.setUserId(request.getUserId());
        waitlist.setTimeSlotId(request.getTimeSlotId());
        waitlist.setStatus("WAITLIST");

        Reservation saved = reservationRepository.save(waitlist);

        long position = reservationRepository.getWaitlistPosition(request.getTimeSlotId(), saved.getId());

        User user = userRepository.findById(request.getUserId()).orElse(null);
        String username = user != null ? user.getUsername() : "用户";

        String title = "候补排队成功";
        String content = String.format("您好 %s，您已成功加入 %s %s-%s 的候补队列，当前排名第 %d 位。",
                username,
                timeSlot.getDate(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                position);
        notificationService.createNotification(request.getUserId(), title, content, saved.getId(), "WAITLIST");

        return saved;
    }

    @Transactional
    public void cancelWaitlist(Long id, Long userId) {
        Reservation waitlist = reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("候补记录不存在"));

        if (!waitlist.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此候补记录");
        }

        if (!"WAITLIST".equals(waitlist.getStatus())) {
            throw new BusinessException("该记录不是候补状态");
        }

        waitlist.setStatus("CANCELLED");
        reservationRepository.save(waitlist);
    }

    public List<ReservationVO> getUserReservations(Long userId) {
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        return reservations.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    public List<ReservationVO> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    public long getWaitlistCount(Long timeSlotId) {
        return reservationRepository.countWaitlistByTimeSlotId(timeSlotId);
    }

    public long getWaitlistPosition(Long timeSlotId, Long reservationId) {
        return reservationRepository.getWaitlistPosition(timeSlotId, reservationId);
    }

    @Transactional
    public void cancelReservation(Long id, Long userId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预约不存在"));

        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException("无权取消此预约");
        }

        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new BusinessException("该预约已取消，无需重复操作");
        }

        if ("WAITLIST".equals(reservation.getStatus())) {
            reservation.setStatus("CANCELLED");
            reservationRepository.save(reservation);
            return;
        }

        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);

        TimeSlot timeSlot = timeSlotRepository.findById(reservation.getTimeSlotId()).orElse(null);
        if (timeSlot != null) {
            timeSlot.setAvailableSeats(timeSlot.getAvailableSeats() + 1);
            timeSlotRepository.save(timeSlot);

            promoteFromWaitlist(timeSlot);
        }
    }

    @Transactional
    protected void promoteFromWaitlist(TimeSlot timeSlot) {
        List<Reservation> waitlist = reservationRepository.findWaitlistByTimeSlotId(timeSlot.getId());
        if (waitlist.isEmpty()) {
            return;
        }

        long availableSeats = timeSlot.getAvailableSeats();
        int promotedCount = 0;

        for (Reservation waitlistItem : waitlist) {
            if (promotedCount >= availableSeats) {
                break;
            }

            long activeCount = reservationRepository.countActiveReservationsByTimeSlotId(timeSlot.getId());
            waitlistItem.setStatus("ACTIVE");
            waitlistItem.setSeatNumber((int) activeCount + 1);
            reservationRepository.save(waitlistItem);

            timeSlot.setAvailableSeats(timeSlot.getAvailableSeats() - 1);
            timeSlotRepository.save(timeSlot);

            User user = userRepository.findById(waitlistItem.getUserId()).orElse(null);
            String username = user != null ? user.getUsername() : "用户";

            StudyRoom room = studyRoomRepository.findById(timeSlot.getStudyRoomId()).orElse(null);
            String roomName = room != null ? room.getName() : "自习室";

            String title = "候补成功！";
            String content = String.format("恭喜 %s，您已成功候补到 %s %s %s-%s 的座位，座位号：%d。",
                    username,
                    roomName,
                    timeSlot.getDate(),
                    timeSlot.getStartTime(),
                    timeSlot.getEndTime(),
                    waitlistItem.getSeatNumber());
            notificationService.createNotification(waitlistItem.getUserId(), title, content, waitlistItem.getId(), "SUCCESS");

            promotedCount++;
        }
    }

    private ReservationVO convertToVO(Reservation reservation) {
        ReservationVO vo = new ReservationVO();
        vo.setId(reservation.getId());
        vo.setUserId(reservation.getUserId());
        vo.setTimeSlotId(reservation.getTimeSlotId());
        vo.setSeatNumber(reservation.getSeatNumber());
        vo.setStatus(reservation.getStatus());
        vo.setCreatedAt(reservation.getCreatedAt());

        userRepository.findById(reservation.getUserId())
                .ifPresent(user -> vo.setUsername(user.getUsername()));

        timeSlotRepository.findById(reservation.getTimeSlotId())
                .ifPresent(timeSlot -> {
                    vo.setStudyRoomId(timeSlot.getStudyRoomId());
                    vo.setDate(timeSlot.getDate());
                    vo.setStartTime(timeSlot.getStartTime());
                    vo.setEndTime(timeSlot.getEndTime());

                    studyRoomRepository.findById(timeSlot.getStudyRoomId())
                            .ifPresent(room -> {
                                vo.setStudyRoomName(room.getName());
                                vo.setLocation(room.getLocation());
                            });

                    if ("WAITLIST".equals(reservation.getStatus())) {
                        long position = reservationRepository.getWaitlistPosition(reservation.getTimeSlotId(), reservation.getId());
                        vo.setWaitlistPosition((int) position);
                    }
                });

        return vo;
    }
}
