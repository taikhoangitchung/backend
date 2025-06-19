package app.dto.exam;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SubmittedQuestion {
    private Long id; // Question ID
    private List<Long> answerIds;
}
