package app.dto.history;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AddHistoryRequest {
    private Long examId;
    private String roomCode;

    private long timeTaken;
    private String finishedAt;
    private List<SubmittedChoice> choices;

    @Setter
    @Getter
    public static class SubmittedChoice {
        private Long questionId;
        private List<Long> answerIds;
    }
}
