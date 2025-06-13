package app.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateExamRequest {
    private String title;
    private String description;
    private Long authorId;
    private List<Long> questionIds;
}
