package app.repository;

import app.entity.Difficulty;
import org.springframework.data.repository.CrudRepository;

public interface DifficultyRepository extends CrudRepository<Difficulty, Long> {
    boolean existsByName(String difficultyName);
}
