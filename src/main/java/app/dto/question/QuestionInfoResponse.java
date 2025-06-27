package app.dto.question;

import app.entity.Answer;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class QuestionInfoResponse {
    private Long id;
    private String content;
    private String category;
    private String type;
    private String difficulty;
    private List<Answer> answers;
    private String image; // Thêm trường để phản hồi đường dẫn ảnh
}