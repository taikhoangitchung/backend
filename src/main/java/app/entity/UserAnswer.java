package app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class UserAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "history_id")
    private History history;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "selected_answer_ids")
    private String selectedAnswerIds;

    @Column(name = "correct_answer_ids")
    private String correctAnswerIds;

    private int score;

    @Transient // Không lưu vào DB, chỉ dùng để nạp dữ liệu
    private List<Answer> answers; // Danh sách đáp án của câu hỏi
}