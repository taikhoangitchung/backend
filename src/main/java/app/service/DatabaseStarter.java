package app.service;

import app.entity.User;
import app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DatabaseStarter implements CommandLineRunner {
    private final UserRepository userRepository;

    @Value("${ADMIN_USERNAME}")
    private String adminUsername;

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${DEFAULT_AVATAR}")
    private String defaultAvatar;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setAvatar(defaultAvatar);
            admin.setPassword(adminPassword);
            admin.setCreateAt(LocalDateTime.now());
            admin.setAdmin(true);
            userRepository.save(admin);
        }
    }
}
