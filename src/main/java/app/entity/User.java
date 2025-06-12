package app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Setter
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean active = true;

    private String fullName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;
//    @ManyToMany(fetch = FetchType.EAGER)
//    private Set<Authority> authorities = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime createAt;

    @Column
    private LocalDateTime lastLogin;
}
