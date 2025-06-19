package app.dto;

import app.entity.UserAnswer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HistoryDTO {
    private Long id;
    private String examName;
    private LocalDateTime completedAt;
    private long timeTaken;
    private long score;
    private long attempts;
    private boolean passed;
    private String username; // Thêm trường username
    private List<UserAnswer> userAnswers; // Thêm danh sách trả lời
}