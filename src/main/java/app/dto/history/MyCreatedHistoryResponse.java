package app.dto.history;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MyCreatedHistoryResponse {
    Long historyId;
    private String examTitle;
    private LocalDateTime finishedAt;
    private long timeTaken;
    private int attemptTime;
    private int countMembers;
}