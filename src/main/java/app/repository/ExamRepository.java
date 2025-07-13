package app.repository;

import app.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    @Query("SELECT e FROM Exam e " +
            "WHERE (" +
            "  (:sourceId = -999) OR " +
            "  (:sourceId != -1 AND e.author.id = :sourceId) OR " +
            "  (:sourceId = -1 AND e.author.id != :currentUserId)" +
            ") " +
            "AND (:categoryId = -1 OR e.category.id = :categoryId) " +
            "AND (:title IS NULL OR :title = '' OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    Page<Exam> findWithFilters(
            @Param("sourceId") Long sourceId,
            @Param("categoryId") Long categoryId,
            @Param("currentUserId") Long currentUserId,
            @Param("title") String title,
            Pageable pageable
    );

    List<Exam> findByCategoryId(Long categoryId);

    boolean existsByTitle(String title);

    void deleteAllQuestionsById(long id);
}