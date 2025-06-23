package app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Exam exam;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User host;

    @ManyToMany
    @JoinTable
    private Set<User> candidates = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Status {
        WAITING,
        STARTED
    }
}
