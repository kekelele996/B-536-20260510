package com.studyroom.service;

import com.studyroom.dto.WaitlistRequest;
import com.studyroom.dto.WaitlistVO;
import com.studyroom.entity.*;
import com.studyroom.exception.BusinessException;
import com.studyroom.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    @Transactional
    public Waitlist joinWaitlist(WaitlistRequest request) {
        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new BusinessException("时间段不存在"));

        if (timeSlot.getAvailableSeats() > 0) {
            throw new BusinessException("该时间段还有可用座位，无需加入候补");
        }

        long userReservationCount = reservationRepository.countActiveReservationsByUserAndTimeSlot(
                request.getUserId(), request.getTimeSlotId());
        if (userReservationCount > 0) {
            throw new BusinessException("您已经预约了该时间段");
        }

        if (waitlistRepository.findByUserIdAndTimeSlotIdAndStatus(
                request.getUserId(), request.getTimeSlotId(), "WAITING").isPresent()) {
            throw new BusinessException("您已在该时间段的候补队列中");
        }

        Integer maxPosition = waitlistRepository.findMaxQueuePositionByTimeSlotId(request.getTimeSlotId());

        Waitlist waitlist = new Waitlist();
        waitlist.setUserId(request.getUserId());
        waitlist.setTimeSlotId(request.getTimeSlotId());
        waitlist.setQueuePosition(maxPosition + 1);
        waitlist.setStatus("WAITING");

        return waitlistRepository.save(waitlist);
    }

    @Transactional
    public void cancelWaitlist(Long id, Long userId) {
        Waitlist waitlist = waitlistRepository.findById(id)
                .orElseThrow(() -> new BusinessException("候补记录不存在"));

        if (!waitlist.getUserId().equals(userId)) {
            throw new BusinessException("无权取消此候补");
        }

        if (!"WAITING".equals(waitlist.getStatus())) {
            throw new BusinessException("该候补已处理，无法取消");
        }

        Integer cancelledPosition = waitlist.getQueuePosition();
        waitlist.setStatus("CANCELLED");
        waitlistRepository.save(waitlist);

        List<Waitlist> remainingWaitlist = waitlistRepository
                .findByTimeSlotIdAndStatusAndQueuePositionGreaterThanOrderByQueuePositionAsc(
                        waitlist.getTimeSlotId(), "WAITING", cancelledPosition);

        for (Waitlist w : remainingWaitlist) {
            w.setQueuePosition(w.getQueuePosition() - 1);
            waitlistRepository.save(w);
        }
    }

    public List<WaitlistVO> getUserWaitlist(Long userId) {
        List<Waitlist> waitlist = waitlistRepository.findByUserId(userId);
        return waitlist.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    public List<WaitlistVO> getTimeSlotWaitlist(Long timeSlotId) {
        List<Waitlist> waitlist = waitlistRepository.findByTimeSlotIdAndStatusOrderByCreatedAtAsc(timeSlotId, "WAITING");
        return waitlist.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Transactional
    public void processWaitlistWhenCancelled(Long timeSlotId) {
        List<Waitlist> waitingList = waitlistRepository
                .findByTimeSlotIdAndStatusOrderByCreatedAtAsc(timeSlotId, "WAITING");

        if (waitingList.isEmpty()) {
            return;
        }

        Waitlist firstWaitlist = waitingList.get(0);
        firstWaitlist.setStatus("CONVERTED");
        waitlistRepository.save(firstWaitlist);

        Reservation reservation = new Reservation();
        reservation.setUserId(firstWaitlist.getUserId());
        reservation.setTimeSlotId(timeSlotId);

        long reservedCount = reservationRepository.countActiveReservationsByTimeSlotId(timeSlotId);
        reservation.setSeatNumber((int) reservedCount + 1);
        reservation.setStatus("ACTIVE");
        Reservation savedReservation = reservationRepository.save(reservation);

        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId).orElse(null);
        if (timeSlot != null) {
            timeSlot.setAvailableSeats(timeSlot.getAvailableSeats() - 1);
            timeSlotRepository.save(timeSlot);

            StudyRoom studyRoom = studyRoomRepository.findById(timeSlot.getStudyRoomId()).orElse(null);
            String studyRoomName = studyRoom != null ? studyRoom.getName() : "自习室";

            notificationService.sendWaitlistSuccessNotification(
                    firstWaitlist.getUserId(),
                    studyRoomName,
                    timeSlot.getDate().toString(),
                    timeSlot.getStartTime().toString(),
                    timeSlot.getEndTime().toString(),
                    savedReservation.getId()
            );
        }

        for (int i = 1; i < waitingList.size(); i++) {
            Waitlist w = waitingList.get(i);
            w.setQueuePosition(w.getQueuePosition() - 1);
            waitlistRepository.save(w);
        }
    }

    private WaitlistVO convertToVO(Waitlist waitlist) {
        WaitlistVO vo = new WaitlistVO();
        vo.setId(waitlist.getId());
        vo.setUserId(waitlist.getUserId());
        vo.setTimeSlotId(waitlist.getTimeSlotId());
        vo.setQueuePosition(waitlist.getQueuePosition());
        vo.setStatus(waitlist.getStatus());
        vo.setCreatedAt(waitlist.getCreatedAt());

        userRepository.findById(waitlist.getUserId())
                .ifPresent(user -> vo.setUsername(user.getUsername()));

        timeSlotRepository.findById(waitlist.getTimeSlotId())
                .ifPresent(timeSlot -> {
                    vo.setStudyRoomId(timeSlot.getStudyRoomId());
                    vo.setDate(timeSlot.getDate().toString());
                    vo.setStartTime(timeSlot.getStartTime().toString());
                    vo.setEndTime(timeSlot.getEndTime().toString());

                    studyRoomRepository.findById(timeSlot.getStudyRoomId())
                            .ifPresent(room -> {
                                vo.setStudyRoomName(room.getName());
                                vo.setLocation(room.getLocation());
                            });
                });

        return vo;
    }

    public boolean isUserInWaitlist(Long userId, Long timeSlotId) {
        return waitlistRepository.findByUserIdAndTimeSlotIdAndStatus(userId, timeSlotId, "WAITING").isPresent();
    }

    public long getWaitlistCount(Long timeSlotId) {
        return waitlistRepository.countByTimeSlotIdAndStatus(timeSlotId, "WAITING");
    }
}
