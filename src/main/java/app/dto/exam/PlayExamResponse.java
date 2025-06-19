package app.dto.exam;

import app.entity.Question;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PlayExamResponse {
    private long duration;
    private List<Question> questions;
}
