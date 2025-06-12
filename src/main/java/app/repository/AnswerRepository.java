package app.repository;

import app.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    void deleteAllByQuestionId(long id);
}
