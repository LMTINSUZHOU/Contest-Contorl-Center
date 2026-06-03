package org.example.contest.config;

import org.example.contest.domain.UserAccount;
import org.example.contest.domain.enums.AccountStatus;
import org.example.contest.domain.enums.UserRole;
import org.example.contest.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 开发环境默认管理员。生产部署时通过 ADMIN_EMAIL/ADMIN_PASSWORD 覆盖。
 */
@Component
public class DataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public DataInitializer(AppProperties appProperties, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.appProperties = appProperties;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void createDefaultAdmin() {
        String email = appProperties.getAdmin().getEmail();
        if (email == null || email.isBlank() || userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        UserAccount admin = new UserAccount();
        admin.setEmail(email.toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(appProperties.getAdmin().getPassword()));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(AccountStatus.NORMAL);
        userRepository.save(admin);
        log.warn("已创建默认管理员账号：{}。生产环境请通过 ADMIN_PASSWORD 修改默认密码。", email);
    }
}
