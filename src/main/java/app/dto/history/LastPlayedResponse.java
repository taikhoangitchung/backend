package app.dto.history;

import app.dto.question.QuestionResultResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class LastPlayedResponse {
    private long correct;
    private long wrong;
    private long timeTaken;
    private long score;
    private List<QuestionResultResponse> questions;
}
