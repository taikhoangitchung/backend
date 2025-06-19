package app.service;

import app.dto.exam.CreateExamRequest;
import app.dto.exam.SubmittedQuestion;
import app.dto.history.HistoryResponse;
import app.entity.Category;
import app.entity.Exam;
import app.entity.History;
import app.entity.User;
import app.entity.UserAnswer;
import app.repository.CategoryRepository;
import app.repository.ExamRepository;
import app.repository.HistoryRepository;
import app.repository.UserAnswerRepository;
import app.repository.UserRepository;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final HistoryRepository historyRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final CategoryRepository categoryRepository;
    private final MessageHelper messageHelper;

    @Transactional
    public void createExam(CreateExamRequest request) {
        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setAuthor(userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new RuntimeException(messageHelper.get("user.not.found"))));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException(messageHelper.get("category.not.found")));
        exam.setCategory(category);

        exam.setDuration(0); // Mặc định
        exam.setPassScore(0); // Mặc định
        exam.setPlayedTimes(0); // Mặc định
        examRepository.save(exam);

        History history = new History();
        history.setUser(exam.getAuthor());
        history.setExam(exam);
        history.setScore(0); // Mặc định
        history.setTimeTaken(0); // Mặc định
        history.setPassed(false); // Mặc định
        history.setFinishedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    public History getExamHistoryDetail(Long historyId, Long userId) {
        return historyRepository.findById(historyId)
                .filter(history -> history.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException(messageHelper.get("history.not.found")));
    }

    public Page<HistoryResponse> getUserHistory(User user, PageRequest pageRequest) {
        Page<History> historyPage = historyRepository.findByUserOrderByFinishedAtDesc(user, pageRequest);
        List<History> allHistories = historyRepository.findByUser(user);

        return historyPage.map(history -> {
            HistoryResponse response = new HistoryResponse();
            response.setId(history.getId());
            response.setTitle(history.getExam().getTitle());
            response.setFinishedAt(history.getFinishedAt());
            response.setTimeTaken(history.getTimeTaken());
            response.setScore(history.getScore());
            response.setAttempts(allHistories.stream()
                    .filter(h -> h.getExam().getId().equals(history.getExam().getId()))
                    .sorted((h1, h2) -> h1.getFinishedAt().compareTo(h2.getFinishedAt()))
                    .collect(Collectors.toList()).indexOf(history) + 1);
            response.setPassed(history.isPassed());
            response.setUsername(history.getUser().getUsername());
            return response;
        });
    }

    public HistoryResponse getHistoryDetailWithAnswers(Long historyId, Long userId) {
        History history = getExamHistoryDetail(historyId, userId);
        HistoryResponse response = new HistoryResponse();
        response.setId(history.getId());
        response.setTitle(history.getExam().getTitle());
        response.setFinishedAt(history.getFinishedAt());
        response.setTimeTaken(history.getTimeTaken());
        response.setScore(history.getScore());
        response.setAttempts(historyRepository.findAllByUserIdAndExamId(userId, history.getExam().getId()).indexOf(history) + 1);
        response.setPassed(history.isPassed());
        response.setUsername(history.getUser().getUsername());

        List<UserAnswer> userAnswers = userAnswerRepository.findByHistoryIdWithDetails(historyId);
        List<SubmittedQuestion> questions = userAnswers.stream().map(ua -> {
            SubmittedQuestion sq = new SubmittedQuestion();
            sq.setId(ua.getQuestion().getId());
            sq.setAnswerIds(ua.getSelectedAnswerIds() != null ?
                    Arrays.stream(ua.getSelectedAnswerIds().split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toList()) : List.of());
            return sq;
        }).collect(Collectors.toList());
        response.setQuestions(questions);

        return response;
    }
}