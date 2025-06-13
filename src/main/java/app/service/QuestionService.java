package app.service;

import app.dto.AddQuestionRequest;
import app.dto.EditQuestionRequest;
import app.dto.QuestionResponse;
import app.entity.*;
import app.exception.DeleteException;
import app.exception.NotFoundException;
import app.repository.*;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final TypeRepository typeRepository;
    private final DifficultyRepository difficultyRepository;
    private final MessageHelper messageHelper;

    @Value("${admin.username}")
    private String adminUsername;

    @Transactional
    public void addQuestion(AddQuestionRequest request) {
        Question question = new Question();
        question.setUser(userRepository.findByUsername(adminUsername));
        question.setCategory(categoryRepository.findByName(request.getCategory()));
        question.setType(typeRepository.findByName(request.getType()));
        question.setDifficulty(difficultyRepository.findByName(request.getDifficulty()));
        question.setContent(request.getContent());
        for (Answer item : request.getAnswers()) {
            item.setQuestion(question);
        }
        question.setAnswers(request.getAnswers());
        questionRepository.save(question);
    }

    public List<QuestionResponse> findByUserId(long userId) {
        List<Question> questions = questionRepository.findByUserId(userId);
        return questions.stream()
                .map(question -> {
                    QuestionResponse response = new QuestionResponse();
                    response.setId(question.getId());
                    response.setContent(question.getContent());
                    response.setCategory(question.getCategory().getName());
                    response.setType(question.getType().getName());
                    response.setDifficulty(question.getDifficulty().getName());
                    response.setAnswers(question.getAnswers());
                    return response;
                })
                .toList();
    }

    public Question findById(long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("question.notFound")));
    }

    public void update(EditQuestionRequest request, long id) {
        Question question = findById(id);
        if (!question.getExams().isEmpty()) {
            throw new DeleteException(messageHelper.get("question.update.conflict"));
        }

        Category category = categoryRepository.findByName(request.getCategory());
        Type type = typeRepository.findByName(request.getType());
        Difficulty difficulty = difficultyRepository.findByName(request.getDifficulty());

        question.setCategory(category);
        question.setType(type);
        question.setDifficulty(difficulty);
        question.setContent(request.getContent());

        for (Answer item : request.getAnswers()) {
            item.setQuestion(question);
        }
        question.setAnswers(request.getAnswers());

        questionRepository.save(question);
    }

    public void delete(long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("question.notFound")));

        if (!question.getExams().isEmpty()) {
            throw new DeleteException(messageHelper.get("question.delete.conflict"));
        }

        questionRepository.delete(question);
        answerRepository.deleteAll(question.getAnswers());
    }
}
