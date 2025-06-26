package app.dto.history;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Rank {
    private int rank;
    private Long historyId;
    private String username;
    private String email;
    private String avatarUrl;
    private double score;
    private long timeTaken;
}
