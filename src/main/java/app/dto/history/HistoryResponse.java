package app.dto.history;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class HistoryResponse {
    private Long id;
    private String examTitle;
    private LocalDateTime finishedAt;
    private String timeTakenFormatted;
    private float scorePercentage;
    private int attemptNumber;
    private String username;
    private boolean passed;
    private List<QuestionDetailResponse> questions;
}