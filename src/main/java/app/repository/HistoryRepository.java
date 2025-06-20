package app.repository;

import app.entity.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {

    History findByExamId(Long id);
    Page<History> findByUserIdOrderByFinishedAtDesc(Long userId, Pageable pageable);
    History findByIdAndUserId(Long id, Long userId);
    List<History> findByExamIdAndUserId(Long examId, Long userId);
}