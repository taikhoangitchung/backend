package app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean isAdmin;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private LocalDateTime createAt;

    @Column
    private LocalDateTime lastLogin;

    @Column(nullable = true, length = 255)
    private String avatar;

    @Column(nullable = true, unique = true)
    private String googleId; // Thêm trường này để lưu ID từ Google
}