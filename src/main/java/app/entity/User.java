package app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private Integer active = 1;

    private String fullName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;
//    @ManyToMany(fetch = FetchType.EAGER)
//    private Set<Authority> authorities = new HashSet<>();
}
