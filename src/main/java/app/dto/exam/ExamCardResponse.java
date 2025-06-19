package app.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ExamCardResponse {
    private Long id;
    private String title;
    private long playedTimes;
    private int questionCount;
}