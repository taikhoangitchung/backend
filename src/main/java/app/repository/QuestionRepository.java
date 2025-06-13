package app.repository;

import app.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("SELECT q FROM Question q WHERE q.user.id = :userId")
    List<Question> findAllByUserId(@Param("userId") long userId);
    long countByCategoryId(Long categoryId);

    boolean existsByCategoryId(Long categoryId);

    List<Question> findByUserId(Long userId);
}
