package app.dto.history;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MyHistoryResponse {
    Long historyId;
    private String examTitle;
    private LocalDateTime finishedAt;
    private long timeTaken;
    private double score;
    private int attemptTime;
    private boolean passed;
}