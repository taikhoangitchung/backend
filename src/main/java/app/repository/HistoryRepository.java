package app.repository;

import app.entity.Exam;
import app.entity.History;
import app.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    List<History> findByUserIdOrderByFinishedAtDesc(Long userId);

    List<History> findByRoomOrderByScoreDesc(Room room);

    List<History> findByExamOrderByUserIdAscFinishedAtAsc(Exam exam);

    List<History> findByRoomOrderByScoreDescTimeTakenAsc(Room room);

    boolean existsByRoom(Room room);
}