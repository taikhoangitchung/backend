package app.dto.history;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class LastPlayedResponse {
    private long CorrectCount;
    private long WrongCount;
    private long timeTaken;
    private long score;
}
