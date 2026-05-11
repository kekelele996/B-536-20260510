package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.dto.LoginRequest;
import com.studyroom.dto.RegisterRequest;
import com.studyroom.entity.User;
import com.studyroom.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        return Result.success("注册成功", data);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request) {
        User user = userService.login(request);
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());
        return Result.success("登录成功", data);
    }

    @GetMapping("/{id}")
    public Result<?> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        user.setPassword(null); // 不返回密码
        return Result.success(user);
    }
}
