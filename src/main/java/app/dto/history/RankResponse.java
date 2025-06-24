package app.dto.history;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class RankResponse {
    private int totalCandidate;
    private List<Rank> rankings;

    @Setter
    @Getter
    @AllArgsConstructor
    public static class Rank {
        private String username;
        private int rank;
    }
}
