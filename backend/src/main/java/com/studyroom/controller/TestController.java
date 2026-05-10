package com.studyroom.controller;

import com.studyroom.common.Result;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/password/{password}")
    public Result<?> generatePassword(@PathVariable String password) {
        String hash = passwordEncoder.encode(password);
        Map<String, String> data = new HashMap<>();
        data.put("password", password);
        data.put("hash", hash);
        data.put("verification", String.valueOf(passwordEncoder.matches(password, hash)));
        return Result.success(data);
    }

    @PostMapping("/verify")
    public Result<?> verifyPassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        String hash = request.get("hash");
        boolean matches = passwordEncoder.matches(password, hash);
        Map<String, Object> data = new HashMap<>();
        data.put("matches", matches);
        data.put("password", password);
        data.put("hash", hash);
        return Result.success(data);
    }

    @GetMapping("/health")
    public Result<?> health() {
        return Result.success("后端运行正常");
    }
}
