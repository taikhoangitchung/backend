package app.dto.history;

import app.dto.exam.SubmittedQuestion;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class HistoryResponse {
    private Long id;
    private String title;
    private LocalDateTime finishedAt;
    private long timeTaken;
    private long score;
    private long attempts;
    private boolean passed;
    private String username;
    private List<SubmittedQuestion> questions;
}