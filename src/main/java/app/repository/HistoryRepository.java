package app.repository;

import app.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Long> {

    History findByExamId(Long id);
}
