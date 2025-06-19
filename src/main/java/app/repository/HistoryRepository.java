package app.repository;

import app.entity.History;
import app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HistoryRepository extends JpaRepository<History, Long> {
    Page<History> findByUserOrderByCompletedAtDesc(User user, Pageable pageable);
    List<History> findByUser(User user);

    @Query("SELECT COUNT(h) FROM History h WHERE h.user.id = :userId AND h.exam.id = :examId")
    long countAttemptsByUserIdAndExamId(Long userId, Long examId);

    Optional<History> findByIdAndUserId(Long id, Long userId);

    List<Object> findAllByUserIdAndExamId(Long userId, Long id);
}