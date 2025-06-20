package app.dto.question;

import app.dto.answer.AnswerResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class QuestionResultResponse {
    private Long id;
    private String content;
    private String type;
    private List<AnswerResponse> answers;
    private List<Long> selectedAnswerIds;
}
