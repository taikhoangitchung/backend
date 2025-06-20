package app.repository;

import app.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    long countByCategoryId(Long categoryId);

    boolean existsByCategoryId(Long categoryId);

    List<Question> findByUserId(long userId);

    @Query("SELECT q FROM Question q " +
            "WHERE (" +
            "  (:userId != -1 AND q.user.id = :userId) " +
            "  OR (:userId = -1 AND q.user.id != -1)" +
            ") " +
            "AND (:categoryId = -1 OR q.category.id = :categoryId)")
    List<Question> findByUserIdAndCategoryIdOptional(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId
    );
}
