package app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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

    @ManyToOne
    @JoinColumn
    private Room room;

    private double score;
    
    private long timeTaken;

    private boolean passed;

    private LocalDateTime finishedAt;

    @OneToMany(mappedBy = "history", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserChoice> userChoices;
}