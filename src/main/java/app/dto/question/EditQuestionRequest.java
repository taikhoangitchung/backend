package app.dto.question;

import app.entity.Answer;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class EditQuestionRequest {
    @NotBlank(message = "{category.required}")
    private String category;

    @NotBlank(message = "{type.required}")
    private String type;

    @NotBlank(message = "{question.content.required}")
    private String content;

    @NotBlank(message = "{difficulty.required}")
    private String difficulty;

    @NotBlank(message = "{answers.required}")
    private List<Answer> answers;
}
