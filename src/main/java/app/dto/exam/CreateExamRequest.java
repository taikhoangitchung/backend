package app.dto.exam;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateExamRequest {
    private String title;
    private Long authorId;
    private long difficultyId;
    private long categoryId;
    private List<Long> questionIds;
    private long duration;
    private long passScore;
    private long playedTimes;
    private Boolean isPublic;
}
