package app.dto.exam;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ExamHistoryDetail {
    private String username;
    private String title;
    private LocalDateTime finishedAt;
    private double score;
    private int correctAnswers;
    private int totalQuestions;
}
