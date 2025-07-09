package app.dto.answer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AnswerRequest {
    private String content;
    private boolean correct;
    // getters/setters
}
