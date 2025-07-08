package app.service;

import app.entity.User;
import app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DatabaseStarter implements CommandLineRunner {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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
        // Tạo tài khoản admin
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setAvatar(defaultAvatar);
            admin.setPassword(passwordEncoder.encode(adminPassword)); // Mã hóa mật khẩu
            admin.setCreateAt(LocalDateTime.now());
            admin.setAdmin(true);
            userRepository.save(admin);
        }

        // Tạo các tài khoản người dùng khác
        createUser("alice", "qweqwe", "alice@example.com");
        createUser("bob", "qweqwe", "bob@example.com");
        createUser("charlie", "qweqwe", "charlie@example.com");
    }

    private void createUser(String username, String password, String email) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setAvatar(defaultAvatar);
            user.setPassword(passwordEncoder.encode(password)); // Mã hóa mật khẩu
            user.setCreateAt(LocalDateTime.now());
            user.setAdmin(false); // Người dùng không phải admin
            userRepository.save(user);
        }
    }
}