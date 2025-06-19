package app.repository;

import app.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    @Query("SELECT ua FROM UserAnswer ua JOIN FETCH ua.question q JOIN FETCH q.answers WHERE ua.history.id = :historyId")
    List<UserAnswer> findByHistoryIdWithDetails(@Param("historyId") Long historyId);
}