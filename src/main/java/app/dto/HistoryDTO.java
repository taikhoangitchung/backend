package app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HistoryDTO {
    private Long id;
    private String examName;
    private LocalDateTime completedAt;
    private long timeTaken;
    private long score;
    private long attempts;
    private boolean passed;
}