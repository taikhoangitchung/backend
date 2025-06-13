package app.service;

import app.entity.User;
import app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
//import quiz.entity.Authority;
//import quiz.repository.AuthorityRepository;

@Service
@RequiredArgsConstructor
public class DatabaseStarter implements CommandLineRunner {
//    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.avatar}")
    private String adminAvatar;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
//        for (Authority.Role role : Authority.Role.values()) {
//            if (!authorityRepository.existsByRole(role)) {
//                Authority authority = new Authority();
//                authority.setRole(role);
//                authorityRepository.save(authority);
//            }
//        }
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setAvatar(adminAvatar);
            admin.setPassword(adminPassword);
            admin.setCreateAt(LocalDateTime.now());
//            Set<Authority> authorities = new HashSet<>();
//            authorities.add(authorityRepository.findByRole(Authority.Role.ADMIN));
//            admin.setAuthorities(authorities);
            userRepository.save(admin);
        }
    }
}
