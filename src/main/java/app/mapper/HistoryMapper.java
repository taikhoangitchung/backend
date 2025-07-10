package app.mapper;


import app.dto.history.MyHistoryResponse;
import app.entity.History;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class HistoryMapper {
    public static MyHistoryResponse toMyHistoryResponse(History history, int attemptOrderByExam) {
        MyHistoryResponse response = new MyHistoryResponse();
        response.setHistoryId(history.getId());
        response.setExamTitle(history.getExam().getTitle());
        response.setFinishedAt(history.getFinishedAt());
        response.setTimeTaken(history.getTimeTaken());
        response.setScore(history.getScore());
        response.setAttemptTime(attemptOrderByExam); // đây là lượt thi theo exam
        response.setPassed(history.isPassed());
        return response;
    }
}