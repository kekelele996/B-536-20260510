package com.studyroom.config;

import com.studyroom.entity.User;
import com.studyroom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 在应用启动时自动创建默认管理员账号（如果不存在）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        initializeDefaultAdmin();
    }

    /**
     * 初始化默认管理员账号
     */
    private void initializeDefaultAdmin() {
        String defaultAdminUsername = "admin";
        String defaultAdminPassword = "admin123";

        // 检查 admin 账号是否存在
        if (!userRepository.existsByUsername(defaultAdminUsername)) {
            log.info("未找到默认管理员账号，正在创建...");
            
            User admin = new User();
            admin.setUsername(defaultAdminUsername);
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setEmail("admin@studyroom.com");
            admin.setRole("ADMIN");
            
            userRepository.save(admin);
            
            log.info("✅ 默认管理员账号创建成功！");
            log.info("   用户名: {}", defaultAdminUsername);
            log.info("   密码: {}", defaultAdminPassword);
        } else {
            log.info("默认管理员账号已存在，跳过初始化");
            
            // 可选：验证密码是否正确
            User admin = userRepository.findByUsername(defaultAdminUsername).orElse(null);
            if (admin != null) {
                boolean passwordMatches = passwordEncoder.matches(defaultAdminPassword, admin.getPassword());
                if (!passwordMatches) {
                    log.warn("⚠️  警告：管理员账号存在，但密码可能不是默认密码 '{}'", defaultAdminPassword);
                    log.warn("⚠️  如需重置密码，请删除 admin 账号后重启应用，或使用注册功能");
                } else {
                    log.info("✅ 管理员账号密码验证通过");
                }
            }
        }
    }
}
