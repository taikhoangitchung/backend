package app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Type type;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Difficulty difficulty;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "question", orphanRemoval = true)
    private List<Answer> answers;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    @ManyToMany(mappedBy = "questions")
    private List<Exam> exams;
}
