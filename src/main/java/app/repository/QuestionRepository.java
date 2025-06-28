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
            "  (:sourceId = -999) OR " +
            "  (:sourceId != -1 AND q.user.id = :sourceId) OR " +
            "  (:sourceId = -1 AND q.user.id != :currentUserId)" +
            ") " +
            "AND (:categoryId = -1 OR q.category.id = :categoryId) " +
            "AND (:username IS NULL OR :username = '' OR LOWER(q.user.username) LIKE LOWER(CONCAT('%', :username, '%')))")
    List<Question> findWithFilters(
            @Param("sourceId") Long sourceId,
            @Param("categoryId") Long categoryId,
            @Param("currentUserId") Long currentUserId,
            @Param("username") String username
    );

    List<Question> findAllByOrderByIdDesc();
}
