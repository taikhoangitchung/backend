package app.service;

import app.entity.Difficulty;
import app.repository.DifficultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DifficultyService {
    private final DifficultyRepository difficultyRepository;

    public void addDifficulty(String difficultyName) {
        if (difficultyRepository.existsByName(difficultyName)) {
            throw new IllegalArgumentException("Difficulty already exists");
        }
        Difficulty difficulty = new Difficulty();
        difficulty.setName(difficultyName);
        difficultyRepository.save(difficulty);
    }
}
