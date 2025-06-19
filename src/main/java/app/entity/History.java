package app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Exam exam;

    private long score;

    private long timeTaken;

    private boolean passed;

    private LocalDateTime finishedAt;
}

    @Column(nullable = false)
    private LocalDateTime completedAt;
}