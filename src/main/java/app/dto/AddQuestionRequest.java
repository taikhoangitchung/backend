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
    private String category;

    @NotBlank(message = "{type.required}")
    private String type;

    @NotBlank(message = "{question.content.required}")
    private String content;

    @NotBlank(message = "{difficulty.required}")
    private String difficulty;

    @NotBlank(message = "{answers.required}")
    private List<Answer> answers;

    @NotBlank
    @Min(value = 1)
    private Long userId; // Assuming the user ID is needed to track who created the question
}
