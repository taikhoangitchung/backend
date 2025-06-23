package app.repository;

import app.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    List<History> findByUserIdOrderByFinishedAtDesc(Long userId);

    @Query("SELECT h.user.id, COUNT(h) FROM History h WHERE h.exam.id = :examId GROUP BY h.user.id")
    List<Object[]> countAttemptsPerUserByExam(@Param("examId") Long examId);

    @Query("SELECT h FROM History h WHERE h.exam.id = :examId ORDER BY h.score DESC, h.timeTaken ASC")
    List<History> findRankedHistoriesByExam(@Param("examId") Long examId);

    List<History> findByExamId(Long examId);
}