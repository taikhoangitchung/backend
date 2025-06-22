package app.dto.history;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class ExamSummaryHistoryResponse {
    private String username;
    private String examTitle;
    private double score;
    private int correctAnswers;
    private int totalQuestions;
    long timeTaken;
    private LocalDateTime finishedAt;
    private long attemptCount;
}
