package app.repository;

import app.entity.Exam;
import app.entity.History;
import app.entity.Room;
import app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    Page<History> findByUserIdOrderByFinishedAtDesc(Long userId, Pageable pageable);

    Page<History> findHistoriesByRoom_HostOrderByFinishedAtDesc(User roomHost, Pageable pageable);

    List<History> findByRoomOrderByScoreDesc(Room room);

    List<History> findByExamOrderByUserIdAscFinishedAtAsc(Exam exam);

    List<History> findByRoomOrderByScoreDescTimeTakenAsc(Room room);

    List<History> findByUserIdOrderByFinishedAtAsc(Long userId);

    List<History> findHistoriesByRoom_HostOrderByFinishedAtAsc(User roomHost);

    boolean existsByRoom(Room room);
}