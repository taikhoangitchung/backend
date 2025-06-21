package app.dto.history;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class HistorySummaryResponse {
    private Long id;
    private String examTitle;
    private String username;
    private LocalDateTime finishedAt;
    private long timeTaken;
    private double score;
    private int attemptTime;
    private boolean passed;
}