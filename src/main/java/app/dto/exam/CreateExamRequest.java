package app.dto.exam;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateExamRequest {
    private String title;
    private Long authorId;
    @NotNull(message = "Category ID is required")
    private Long categoryId;
}