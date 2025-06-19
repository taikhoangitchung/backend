package app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn( nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Exam exam;

    private long timeTaken;
    private boolean passed;

    @Column
    private LocalDateTime finishedAt;
}