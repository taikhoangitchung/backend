package app.dto;

import app.entity.Answer;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class QuestionResponse {
    private Long id;
    private String content;
    private String category;
    private String type;
    private String difficulty;
    private List<Answer> answers;
}
