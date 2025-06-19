package app.dto.history;

import app.dto.exam.SubmittedQuestion;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AddHistoryRequest {
    private Long examId;
    private Long userId;
    private long timeTaken;
    private String finishedAt;
    private List<SubmittedQuestion> questions;
}
