package app.repository;

import app.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    long countByCategoryId(Long categoryId);

    boolean existsByCategoryId(Long categoryId);

    List<Question> findByUserId(Long userId);
}
