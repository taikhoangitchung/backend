package app.dto.history;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionDetailResponse {
    private Long id;
    private String content;
    private String correctAnswers;
    private String selectedAnswers;
}