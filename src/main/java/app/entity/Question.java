package app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

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
    @JsonIgnore
    private List<Exam> exams;

    private String image;

    public List<Long> getCorrectAnswerIds() {
        return this.getAnswers().stream()
                .filter(Answer::getCorrect)
                .map(Answer::getId)
                .collect(Collectors.toList());
    }
}