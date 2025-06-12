package app.service;

import app.dto.AddDifficultyRequest;
import app.entity.Difficulty;
import app.repository.DifficultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DifficultyService {
    private final DifficultyRepository difficultyRepository;

    public void addDifficulty(AddDifficultyRequest request) {
        if (difficultyRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Difficulty already exists");
        }
        Difficulty difficulty = new Difficulty();
        difficulty.setName(request.getName());
        difficultyRepository.save(difficulty);
    }

    public List<Difficulty> getAllDifficulties() {
        return difficultyRepository.findAll();
    }
}
