package app.repository;

import app.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    History findByExamId(Long id);
    List<History> findByUserIdOrderByFinishedAtDesc(Long userId);
    History findByIdAndUserId(Long id, Long userId);
    List<History> findByExamIdAndUserId(Long examId, Long userId);
}