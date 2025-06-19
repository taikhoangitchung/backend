package app.service;

import app.dto.difficulty.AddDifficultyRequest;
import app.entity.Difficulty;
import app.exception.DuplicateException;
import app.repository.DifficultyRepository;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DifficultyService {
    private final DifficultyRepository difficultyRepository;
    private final MessageHelper messageHelper;

    public void addDifficulty(AddDifficultyRequest request) {
        if (difficultyRepository.existsByName(request.getName())) {
            throw new DuplicateException(messageHelper.get("difficulty.exists"));
        }
        Difficulty difficulty = new Difficulty();
        difficulty.setName(request.getName());
        difficultyRepository.save(difficulty);
    }

    public List<Difficulty> getAllDifficulties() {
        return difficultyRepository.findAll();
    }
}
