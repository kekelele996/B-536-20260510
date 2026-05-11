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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final WaitlistService waitlistService;

    @Transactional
    public Reservation createReservation(ReservationRequest request) {
        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new BusinessException("时间段不存在"));

        // 检查用户是否已经预约了这个时间段
        long userReservationCount = reservationRepository.countActiveReservationsByUserAndTimeSlot(
                request.getUserId(), request.getTimeSlotId());
        if (userReservationCount > 0) {
            throw new BusinessException("您已经预约了该时间段，请勿重复预约");
        }

        // 检查时间段是否已满
        long reservedCount = reservationRepository.countActiveReservationsByTimeSlotId(request.getTimeSlotId());
        if (reservedCount >= timeSlot.getAvailableSeats()) {
            throw new BusinessException("该时间段已满，无法预约");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(request.getUserId());
        reservation.setTimeSlotId(request.getTimeSlotId());
        reservation.setSeatNumber((int) reservedCount + 1);
        reservation.setStatus("ACTIVE");

        Reservation saved = reservationRepository.save(reservation);

        // 减少时间段的可用座位数
        timeSlot.setAvailableSeats(timeSlot.getAvailableSeats() - 1);
        timeSlotRepository.save(timeSlot);

        return saved;
    }

    public List<ReservationVO> getUserReservations(Long userId) {
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        return reservations.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    public List<ReservationVO> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream().map(this::convertToVO).collect(Collectors.toList());
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

        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);

        TimeSlot timeSlot = timeSlotRepository.findById(reservation.getTimeSlotId())
                .orElse(null);
        if (timeSlot != null) {
            timeSlot.setAvailableSeats(timeSlot.getAvailableSeats() + 1);
            timeSlotRepository.save(timeSlot);
        }

        waitlistService.processWaitlistWhenCancelled(reservation.getTimeSlotId());
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
                });

        return vo;
    }
}
