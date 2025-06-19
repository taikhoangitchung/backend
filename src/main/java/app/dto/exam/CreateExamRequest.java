package app.dto.exam;

import lombok.Data;

@Data
public class CreateExamRequest {
    private String title;
    private Long authorId;
    private Long categoryId;
}