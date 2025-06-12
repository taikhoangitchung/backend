package app.dto;

import app.entity.Answer;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AddQuestionRequest {
    @NotBlank(message = "{category.required}")
    @Min(value = 1, message = "{categoryId.invalid}")
    private Long categoryId;

    @NotBlank(message = "{type.required}")
    @Min(value = 1, message = "{typeId.invalid}")
    private Long typeId;

    @NotBlank(message = "{question.content.required}")
    private String content;

    @NotBlank(message = "{difficulty.required}")
    @Min(value = 1, message = "{difficultyId.invalid}")
    private Long difficultyId;

    @NotBlank(message = "{answers.required}")
    private List<Answer> answers;
}
