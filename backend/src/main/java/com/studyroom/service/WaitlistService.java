package com.studyroom.service;

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
    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public WaitlistVO joinWaitlist(Long userId, Long timeSlotId) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new BusinessException("时间段不存在"));

        long userReservationCount = reservationRepository.countActiveReservationsByUserAndTimeSlot(userId, timeSlotId);
        if (userReservationCount > 0) {
            throw new BusinessException("您已预约该时间段，无需候补");
        }

        long alreadyWaiting = waitlistRepository.countWaitingByUserAndTimeSlot(userId, timeSlotId);
        if (alreadyWaiting > 0) {
            throw new BusinessException("您已在候补队列中，请勿重复加入");
        }

        long reservedCount = reservationRepository.countActiveReservationsByTimeSlotId(timeSlotId);
        if (reservedCount < timeSlot.getAvailableSeats()) {
            throw new BusinessException("该时间段还有余位，请直接预约");
        }

        long waitingCount = waitlistRepository.countWaitingByTimeSlotId(timeSlotId);

        Waitlist waitlist = new Waitlist();
        waitlist.setUserId(userId);
        waitlist.setTimeSlotId(timeSlotId);
        waitlist.setPosition((int) waitingCount + 1);
        waitlist.setStatus("WAITING");

        Waitlist saved = waitlistRepository.save(waitlist);
        return convertToVO(saved);
    }

    @Transactional
    public void leaveWaitlist(Long id, Long userId) {
        Waitlist waitlist = waitlistRepository.findById(id)
                .orElseThrow(() -> new BusinessException("候补记录不存在"));

        if (!waitlist.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此候补记录");
        }

        if (!"WAITING".equals(waitlist.getStatus())) {
            throw new BusinessException("该候补记录已处理，无法退出");
        }

        waitlist.setStatus("CANCELLED");
        waitlistRepository.save(waitlist);

        reorderPositions(waitlist.getTimeSlotId());
    }

    public List<WaitlistVO> getUserWaitlist(Long userId) {
        List<Waitlist> waitlists = waitlistRepository.findByUserId(userId);
        return waitlists.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    public long getWaitlistCount(Long timeSlotId) {
        return waitlistRepository.countWaitingByTimeSlotId(timeSlotId);
    }

    public boolean isUserWaiting(Long userId, Long timeSlotId) {
        return waitlistRepository.countWaitingByUserAndTimeSlot(userId, timeSlotId) > 0;
    }

    @Transactional
    public void promoteNext(Long timeSlotId) {
        List<Waitlist> waitingList = waitlistRepository.findWaitingByTimeSlotId(timeSlotId);
        if (waitingList.isEmpty()) {
            return;
        }

        Waitlist next = waitingList.get(0);

        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElse(null);
        if (timeSlot == null) {
            return;
        }

        if (timeSlot.getAvailableSeats() <= 0) {
            return;
        }

        long reservedCount = reservationRepository.countActiveReservationsByTimeSlotId(timeSlotId);

        Reservation reservation = new Reservation();
        reservation.setUserId(next.getUserId());
        reservation.setTimeSlotId(timeSlotId);
        reservation.setSeatNumber((int) reservedCount + 1);
        reservation.setStatus("ACTIVE");
        reservationRepository.save(reservation);

        timeSlot.setAvailableSeats(timeSlot.getAvailableSeats() - 1);
        timeSlotRepository.save(timeSlot);

        next.setStatus("CONVERTED");
        waitlistRepository.save(next);

        String roomName = "";
        String dateStr = timeSlot.getDate().toString();
        String timeStr = timeSlot.getStartTime() + "-" + timeSlot.getEndTime();
        studyRoomRepository.findById(timeSlot.getStudyRoomId())
                .ifPresent(room -> roomName = room.getName());

        Notification notification = new Notification();
        notification.setUserId(next.getUserId());
        notification.setMessage("您候补的 " + roomName + " " + dateStr + " " + timeStr + " 已有空位，已为您自动预约成功！");
        notification.setType("WAITLIST_AUTO_RESERVED");
        notification.setRead(false);
        notification.setRelatedId(reservation.getId());
        notificationRepository.save(notification);

        reorderPositions(timeSlotId);
    }

    private void reorderPositions(Long timeSlotId) {
        List<Waitlist> waitingList = waitlistRepository.findWaitingByTimeSlotId(timeSlotId);
        for (int i = 0; i < waitingList.size(); i++) {
            waitingList.get(i).setPosition(i + 1);
            waitlistRepository.save(waitingList.get(i));
        }
    }

    private WaitlistVO convertToVO(Waitlist waitlist) {
        WaitlistVO vo = new WaitlistVO();
        vo.setId(waitlist.getId());
        vo.setUserId(waitlist.getUserId());
        vo.setTimeSlotId(waitlist.getTimeSlotId());
        vo.setPosition(waitlist.getPosition());
        vo.setStatus(waitlist.getStatus());
        vo.setCreatedAt(waitlist.getCreatedAt());

        userRepository.findById(waitlist.getUserId())
                .ifPresent(user -> vo.setUsername(user.getUsername()));

        timeSlotRepository.findById(waitlist.getTimeSlotId())
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
