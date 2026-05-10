package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.entity.StudyRoom;
import com.studyroom.service.StudyRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study-rooms")
@RequiredArgsConstructor
public class StudyRoomController {

    private final StudyRoomService studyRoomService;

    @GetMapping
    public Result<List<StudyRoom>> getAllStudyRooms() {
        List<StudyRoom> rooms = studyRoomService.getAllStudyRooms();
        return Result.success(rooms);
    }

    @GetMapping("/{id}")
    public Result<StudyRoom> getStudyRoomById(@PathVariable Long id) {
        StudyRoom room = studyRoomService.getStudyRoomById(id);
        return Result.success(room);
    }

    @PostMapping
    public Result<StudyRoom> createStudyRoom(@RequestBody StudyRoom studyRoom) {
        StudyRoom created = studyRoomService.createStudyRoom(studyRoom);
        return Result.success("创建成功", created);
    }

    @PutMapping("/{id}")
    public Result<StudyRoom> updateStudyRoom(@PathVariable Long id, @RequestBody StudyRoom studyRoom) {
        StudyRoom updated = studyRoomService.updateStudyRoom(id, studyRoom);
        return Result.success("更新成功", updated);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteStudyRoom(@PathVariable Long id) {
        studyRoomService.deleteStudyRoom(id);
        return Result.success("删除成功", null);
    }
}
