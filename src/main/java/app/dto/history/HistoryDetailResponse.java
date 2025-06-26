package app.dto.history;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class HistoryDetailResponse {
    private String username;
    private String avatarUrl;
    private int rank;
    private int correct;
    private int wrong;
    private long timeTaken;
    private double score;
    private List<ChoiceResult> choices;
    private List<QuestionDTO> fullQuestions;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ChoiceResult {
        private Long questionId;
        private List<Long> selectedAnswerIds;
        private List<Long> correctAnswerIds;
        private boolean isCorrect;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class QuestionDTO {
        private Long id;
        private String content;
        private List<AnswerDTO> answers;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AnswerDTO {
        private Long id;
        private String content;
        private boolean correct;
    }
}
