package app.service;

import app.dto.answer.AnswerRequest;
import app.dto.question.*;
import app.entity.*;
import app.exception.*;
import app.repository.*;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

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

    @Value("${APP_UPLOAD_DIR}")
    private String uploadDirectory;

    @Value("${UPLOAD_URL_PREFIX}")
    private String urlPrefix;

    @Value("${ADMIN_USERNAME}")
    private String adminUsername;

    public Page<Question> getAll(Pageable pageable) { // Thay List bằng Page
        return questionRepository.findAllByOrderByIdDesc(pageable);
    }

    @Transactional
    public void storeQuestion(QuestionRequest request) {
        Question question = new Question();

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User foundUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
        question.setUser(foundUser);

        resolveAndSetBasicFields(question, request);

        List<Answer> answers = mapAnswers(request.getAnswers(), question);
        question.setAnswers(answers);

        questionRepository.save(question);
    }

    @Transactional
    public void addAllQuestionFromExcel(AddQuestionFromExcel request) {
        User foundUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));

        List<Question> questions = new ArrayList<>();
        try {
            request.getQuestions().forEach((data) -> {
                Question question = new Question();
                question.setContent(data.getContent());

                Difficulty difficulty = difficultyRepository.findByName(data.getDifficulty().trim());
                if (difficulty == null) throw new NotFoundException(messageHelper.get("difficulty.not.found"));
                question.setDifficulty(difficulty);

                Category category = categoryRepository.findByName(data.getCategory().trim());
                if (category == null) throw new NotFoundException(messageHelper.get("category.not.found"));
                question.setCategory(category);

                Type type = typeRepository.findByName(data.getType().trim());
                if (type == null) throw new NotFoundException(messageHelper.get("type.not.found"));
                question.setType(type);

                question.setUser(foundUser);
                question.setImage(null);
                question.setExams(new ArrayList<>());

                List<Answer> answers = new ArrayList<>();
                data.getAnswers().forEach((dataAnswer) -> {
                    Answer answer = new Answer();
                    answer.setContent(dataAnswer.getContent());
                    answer.setCorrect(dataAnswer.getCorrect());
                    answer.setQuestion(question);
                    answers.add(answer);
                });

                question.setAnswers(answers);

                long correctCount = answers.stream().filter(Answer::getCorrect).count();

                if (question.getType().getName().equals("multiple")) {
                    if (correctCount < 2) {
                        throw new AnswerException(messageHelper.get("multiple.answer.invalid"));
                    }
                    if (answers.size() != 4) {
                        throw new AnswerException(messageHelper.get("number.answer.invalid"));
                    }
                }

                if (question.getType().getName().equals("single")) {
                    if (correctCount > 1 || correctCount == 0) {
                        throw new AnswerException(messageHelper.get("single.answer.invalid"));
                    }
                    if (answers.size() != 4) {
                        throw new AnswerException(messageHelper.get("number.answer.invalid"));
                    }
                }

                if (question.getType().getName().equals("boolean")) {
                    if (answers.size() != 2) {
                        throw new AnswerException(messageHelper.get("boolean.answer.invalid"));
                    }
                    if (correctCount != 1) {
                        throw new AnswerException(messageHelper.get("boolean.answer.correct.invalid"));
                    }
                }

                questions.add(question);
            });
        } catch (Exception ex) {
            throw new ExcelImportException(ex.getMessage());
        }

        questionRepository.saveAll(questions);
    }

    public List<QuestionInfoResponse> findByUserId(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(messageHelper.get("user.not.found"));
        }
        List<Question> questions = questionRepository.findByUserId(userId);
        return questions.stream()
                .map(question -> {
                    QuestionInfoResponse response = new QuestionInfoResponse();
                    response.setId(question.getId());
                    response.setContent(question.getContent());
                    response.setCategory(question.getCategory().getName());
                    response.setType(question.getType().getName());
                    response.setDifficulty(question.getDifficulty().getName());
                    response.setAnswers(question.getAnswers());
                    if (question.getImage() != null) {
                        response.setImage(question.getImage());
                    }
                    return response;
                })
                .toList();
    }

    public Question findById(long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("question.not.found")));
    }

    @Transactional
    public void updateQuestion(QuestionRequest request, long id) {
        Question question = findById(id);

        resolveAndSetBasicFields(question, request);

        question.getAnswers().clear(); // Xoá các câu trả lời cũ (nếu dùng orphanRemoval)

        List<Answer> answers = mapAnswers(request.getAnswers(), question);
        question.getAnswers().addAll(answers);

        questionRepository.save(question);
    }

    public void delete(long id) {
        Question question = findById(id);

        if (!question.getExams().isEmpty()) {
            throw new LockedException(messageHelper.get("question.delete.conflict"));
        }

        if (question.getImage() != null) {
            try {
                Path path = Paths.get(uploadDirectory, question.getImage().replace(urlPrefix, "").replaceFirst("/", ""));
                Files.deleteIfExists(path);
            } catch (IOException e) {
                // Log error but continue deletion
            }
        }
        questionRepository.delete(question);
        answerRepository.deleteAll(question.getAnswers());
    }

    public Page<Question> findWithFilters(FilterQuestionRequest request, Pageable pageable) { // Thay List bằng Page
        return questionRepository.findWithFilters(
                request.getSourceId(),
                request.getCategoryId(),
                request.getCurrentUserId(),
                request.getUsername(),
                pageable
        );
    }

    public void ensureEditable(Question question) {
        if (!question.getExams().isEmpty()) {
            throw new LockedException(messageHelper.get("question.update.conflict"));
        }
    }

    private void resolveAndSetBasicFields(Question question, QuestionRequest request) {
        Category category = categoryRepository.findByName(request.getCategory());
        if (category == null) throw new NotFoundException(messageHelper.get("category.not.found"));
        question.setCategory(category);

        Type type = typeRepository.findByName(request.getType());
        if (type == null) throw new NotFoundException(messageHelper.get("type.not.found"));
        question.setType(type);

        Difficulty difficulty = difficultyRepository.findByName(request.getDifficulty());
        if (difficulty == null) throw new NotFoundException(messageHelper.get("difficulty.not.found"));
        question.setDifficulty(difficulty);

        question.setContent(request.getContent());

        if (request.getImage() != null && !request.getImage().isBlank()) {
            question.setImage(request.getImage());
        }
    }

    private List<Answer> mapAnswers(List<AnswerRequest> answerRequests, Question question) {
        return answerRequests.stream()
                .map(dto -> {
                    Answer answer = new Answer();
                    answer.setContent(dto.getContent());
                    answer.setCorrect(dto.isCorrect());
                    answer.setQuestion(question);
                    return answer;
                })
                .collect(Collectors.toList());
    }


}