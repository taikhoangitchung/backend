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

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${default.avatar}")
    private String defaultAvatar;

    @Value("${admin.password}")
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
