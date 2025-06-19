package app.repository;

import app.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.history.id = :historyId")
    List<UserAnswer> findByHistoryId(@Param("historyId") Long historyId);

    @Query("SELECT COUNT(*) FROM UserAnswer ua WHERE ua.history.id = :historyId AND ua.correctAnswerIds = ua.selectedAnswerIds")
    long countCorrectAnswersByHistoryId(@Param("historyId") Long historyId);
}