package app.service;

import app.dto.CreateExamRequest;
import app.dto.HistoryDTO;
import app.entity.Exam;
import app.entity.History;
import app.entity.User;
import app.entity.UserAnswer;
import app.repository.ExamRepository;
import app.repository.HistoryRepository;
import app.repository.UserAnswerRepository;
import app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final HistoryRepository historyRepository;
    private final UserAnswerRepository userAnswerRepository;

    @Transactional
    public void createExam(CreateExamRequest request) {
        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setAuthor(userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new RuntimeException("User not found")));
        exam.setDuration(0); // Mặc định, cần điều chỉnh
        exam.setPassScore(0); // Mặc định, cần điều chỉnh
        exam.setPlayedTimes(0); // Mặc định
        examRepository.save(exam);

        History history = new History();
        history.setUser(exam.getAuthor());
        history.setExam(exam);
        history.setScore(0); // Mặc định
        history.setTimeTaken(0); // Mặc định
        history.setPassed(false); // Mặc định
        history.setCompletedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    public History getExamHistoryDetail(Long historyId, Long userId) {
        return historyRepository.findByIdAndUserId(historyId, userId)
                .orElseThrow(() -> new RuntimeException("History not found"));
    }

    public Page<HistoryDTO> getUserHistory(User user, PageRequest pageRequest) {
        Page<History> historyPage = historyRepository.findByUserOrderByCompletedAtDesc(user, pageRequest);
        List<History> allHistories = historyRepository.findByUser(user);

        return historyPage.map(history -> {
            HistoryDTO dto = new HistoryDTO();
            dto.setId(history.getId());
            dto.setExamName(history.getExam().getTitle());
            dto.setCompletedAt(history.getCompletedAt());
            dto.setTimeTaken(history.getTimeTaken());
            dto.setScore(history.getScore());

            List<History> examHistories = allHistories.stream()
                    .filter(h -> h.getExam().getId().equals(history.getExam().getId()))
                    .sorted((h1, h2) -> h1.getCompletedAt().compareTo(h2.getCompletedAt()))
                    .collect(Collectors.toList());
            long attemptOrder = examHistories.indexOf(history) + 1;
            dto.setAttempts(attemptOrder);
            dto.setPassed(history.isPassed());
            return dto;
        });
    }

    public HistoryDTO getHistoryDetailWithAnswers(Long historyId, Long userId) {
        History history = getExamHistoryDetail(historyId, userId);
        HistoryDTO dto = new HistoryDTO();
        dto.setId(history.getId());
        dto.setExamName(history.getExam().getTitle());
        dto.setCompletedAt(history.getCompletedAt());
        dto.setTimeTaken(history.getTimeTaken());
        dto.setScore(history.getScore());
        dto.setAttempts(historyRepository.findAllByUserIdAndExamId(userId, history.getExam().getId()).indexOf(history) + 1);
        dto.setPassed(history.isPassed());

        // Lấy thông tin user
        User user = history.getUser();
        dto.setUsername(user.getUsername());

        // Lấy lịch sử trả lời và nạp quan hệ
        List<UserAnswer> userAnswers = userAnswerRepository.findByHistoryId(historyId);
        for (UserAnswer ua : userAnswers) {
            ua.setQuestion(ua.getQuestion()); // Đảm bảo nạp question
            ua.setAnswers(ua.getQuestion().getAnswers()); // Nạp danh sách đáp án
        }
        dto.setUserAnswers(userAnswers);

        return dto;
    }
}