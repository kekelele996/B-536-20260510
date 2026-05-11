package com.studyroom.service;

import com.studyroom.entity.StudyRoom;
import com.studyroom.exception.BusinessException;
import com.studyroom.repository.StudyRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyRoomService {

    private final StudyRoomRepository studyRoomRepository;

    public List<StudyRoom> getAllStudyRooms() {
        return studyRoomRepository.findByStatus("ACTIVE");
    }

    public StudyRoom getStudyRoomById(Long id) {
        return studyRoomRepository.findById(id)
                .orElseThrow(() -> new BusinessException("自习室不存在"));
    }

    @Transactional
    public StudyRoom createStudyRoom(StudyRoom studyRoom) {
        return studyRoomRepository.save(studyRoom);
    }

    @Transactional
    public StudyRoom updateStudyRoom(Long id, StudyRoom studyRoom) {
        StudyRoom existing = getStudyRoomById(id);
        existing.setName(studyRoom.getName());
        existing.setLocation(studyRoom.getLocation());
        existing.setCapacity(studyRoom.getCapacity());
        existing.setDescription(studyRoom.getDescription());
        existing.setStatus(studyRoom.getStatus());
        return studyRoomRepository.save(existing);
    }

    @Transactional
    public void deleteStudyRoom(Long id) {
        StudyRoom studyRoom = getStudyRoomById(id);
        studyRoom.setStatus("INACTIVE");
        studyRoomRepository.save(studyRoom);
    }
}
