package app.dto.exam;


import app.entity.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ExamSummaryResponse {
    private Long id;
    private String title;
    private int questionCount;
    private Difficulty difficulty;
    private long playedTimes;
    private boolean isPublic;
    private Long authorId;
}