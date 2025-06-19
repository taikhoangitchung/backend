package app.repository;

import app.entity.History;
import app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long> {
    @EntityGraph(attributePaths = {"user", "exam", "userAnswers", "userAnswers.question", "userAnswers.answers"})
    List<History> findByUser(User user);

    @EntityGraph(attributePaths = {"user", "exam", "userAnswers", "userAnswers.question", "userAnswers.answers"})
    Page<History> findByUserOrderByFinishedAtDesc(User user, Pageable pageable);

    List<History> findAllByUserIdAndExamId(Long userId, Long examId);
}