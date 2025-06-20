package app.dto.history;

import app.dto.answer.AnswerResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QuestionDetailResponse {
    private Long id;
    private String content;
    private List<AnswerResponse> answers = new ArrayList<>();
    private List<Long> selectedAnswerIds = new ArrayList<>();
}