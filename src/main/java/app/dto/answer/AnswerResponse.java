package app.dto.answer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AnswerResponse {
    private Long id;
    private String content;
    private boolean correct;
    private String color;
}
