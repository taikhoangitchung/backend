package app.dto.question;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterQuestionRequest {
    private long categoryId;
    private long sourceId;
    private long currentUserId;
    private String username;
}