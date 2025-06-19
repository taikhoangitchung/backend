package app.service;

import app.dto.history.HistoryResponse;
import app.dto.history.QuestionDetailResponse;
import app.entity.History;
import app.entity.UserAnswer;
import app.repository.HistoryRepository;
import app.repository.UserAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;
    private final UserAnswerRepository userAnswerRepository;

    public Page<HistoryResponse> getHistoryByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<History> historyPage = historyRepository.findByUserIdOrderByFinishedAtDesc(userId, pageable);
        return historyPage.map(this::convertToResponse);
    }

    public HistoryResponse getHistoryDetail(Long userId, Long historyId) {
        History history = historyRepository.findByIdAndUserId(historyId, userId);
        if (history == null) {
            throw new IllegalArgumentException("Không tìm thấy lịch sử bài thi hoặc bạn không có quyền truy cập.");
        }
        HistoryResponse response = convertToResponse(history);
        response.setQuestions(getQuestionDetails(historyId));
        return response;
    }

    private HistoryResponse convertToResponse(History history) {
        HistoryResponse response = new HistoryResponse();
        response.setId(history.getId());
        response.setExamTitle(history.getExam().getTitle());
        response.setFinishedAt(history.getFinishedAt());
        response.setTimeTakenFormatted(formatTimeTaken(history.getTimeTaken()));
        response.setScorePercentage(calculateScorePercentage(history));
        response.setUsername(history.getUser().getUsername());
        response.setPassed(history.isPassed());

        List<History> allAttempts = historyRepository.findByExamIdAndUserId(history.getExam().getId(), history.getUser().getId());
        allAttempts.sort(Comparator.comparing(History::getFinishedAt));
        int attemptNumber = allAttempts.indexOf(history) + 1;
        response.setAttemptNumber(attemptNumber);

        response.setQuestions(getQuestionDetails(history.getId()));
        return response;
    }

    private float calculateScorePercentage(History history) {
        long correctAnswers = getCorrectAnswersCount(history.getId());
        long totalQuestions = (history.getExam() != null && history.getExam().getQuestions() != null) ? history.getExam().getQuestions().size() : 0;
        return totalQuestions > 0 ? (float) (correctAnswers * 100) / totalQuestions : 0.0f;
    }

    private long getCorrectAnswersCount(Long historyId) {
        if (historyId == null) return 0;
        return userAnswerRepository.countCorrectAnswersByHistoryId(historyId);
    }

    private List<QuestionDetailResponse> getQuestionDetails(Long historyId) {
        List<UserAnswer> userAnswers = userAnswerRepository.findByHistoryId(historyId);
        List<QuestionDetailResponse> questions = new ArrayList<>();
        for (UserAnswer ua : userAnswers) {
            QuestionDetailResponse qdr = new QuestionDetailResponse();
            qdr.setId(ua.getQuestion().getId());
            qdr.setContent(ua.getQuestion().getContent());
            qdr.setCorrectAnswers(ua.getCorrectAnswerIds());
            qdr.setSelectedAnswers(ua.getSelectedAnswerIds());
            questions.add(qdr);
        }
        return questions;
    }

    private String formatTimeTaken(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
}