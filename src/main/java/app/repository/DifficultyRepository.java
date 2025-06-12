package app.repository;

import app.entity.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DifficultyRepository extends JpaRepository<Difficulty, Long> {
    boolean existsByName(String difficultyName);
    Difficulty findByName(String difficultyName);
}
