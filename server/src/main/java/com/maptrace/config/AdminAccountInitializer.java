package com.maptrace.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maptrace.mapper.AdminAccountMapper;
import com.maptrace.model.entity.AdminAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {

    private final AdminAccountMapper adminAccountMapper;

    @Value("${admin.security.init-password:#{null}}")
    private String initPassword;

    @Override
    public void run(String... args) {
        try {
            long count = adminAccountMapper.selectCount(
                    new LambdaQueryWrapper<AdminAccount>().eq(AdminAccount::getUsername, "admin"));
            if (count == 0) {
                String password = (initPassword != null && !initPassword.isBlank()) ? initPassword : generateRandomPassword();
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                AdminAccount account = new AdminAccount();
                account.setUsername("admin");
                account.setPasswordHash(encoder.encode(password));
                account.setNickname("超级管理员");
                account.setRole("super_admin");
                account.setIsEnabled(1);
                account.setMustChangePassword(1);
                account.setLoginFailCount(0);
                account.setPasswordHistory("[\"" + account.getPasswordHash() + "\"]");
                adminAccountMapper.insert(account);
                log.info("========================================");
                log.info("默认管理员账号已创建: admin");
                log.info("初始密码: {}", password);
                log.info("首次登录后必须修改密码");
                log.info("========================================");
            }
        } catch (Exception e) {
            log.warn("初始化管理员账号失败（表可能不存在）: {}", e.getMessage());
        }
    }

    /**
     * 生成随机强密码（16位，包含大小写字母、数字、特殊字符）
     */
    private String generateRandomPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%&*";
        String all = upper + lower + digits + special;
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(16);
        // 确保每种字符至少一个
        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(special.charAt(random.nextInt(special.length())));
        for (int i = 4; i < 16; i++) {
            sb.append(all.charAt(random.nextInt(all.length())));
        }
        // 打乱顺序
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }
}
