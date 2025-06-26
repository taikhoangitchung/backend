package app.dto.exam;

import app.entity.Question;
import app.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Setter
@Getter
public class PlayExamResponse {
    private long duration;
    private List<Question> questions;
    private Set<User> candidates;
}
