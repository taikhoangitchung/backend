package app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;

@Entity
@Getter
@Setter
public class UserChoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private History history;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Question question;

    @ElementCollection
    private List<Long> selectedAnswerIds;

    public boolean isCorrect() {
        List<Long> correctIds = question.getCorrectAnswerIds();
        List<Long> selected = selectedAnswerIds;

        return selected.size() == correctIds.size() &&
                new HashSet<>(selected).containsAll(correctIds);
    }
}