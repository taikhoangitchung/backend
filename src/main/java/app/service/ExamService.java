package app.service;

import app.dto.CreateExamRequest;
import app.dto.HistoryDTO;
import app.entity.Exam;
import app.entity.History;
import app.entity.User;
import app.repository.ExamRepository;
import app.repository.HistoryRepository;
import app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final HistoryRepository historyRepository;

    public History getExamHistoryDetail(Long userId, Long examId) {
        return historyRepository.findByUserIdAndExamId(userId, examId)
                .orElseThrow(() -> new RuntimeException("History not found"));
    }

    @Transactional
    public void createExam(CreateExamRequest request) {
        // Tạo một bài thi mới
        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setAuthor(userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new RuntimeException("User not found")));
        // Thiết lập các trường khác như difficulty, questions, duration, passScore nếu cần
        exam.setDuration(0); // Mặc định, cần điều chỉnh
        exam.setPassScore(0); // Mặc định, cần điều chỉnh
        exam.setPlayedTimes(0); // Mặc định
        examRepository.save(exam);

        // Tạo lịch sử thi ban đầu (nếu cần)
        History history = new History();
        history.setUser(exam.getAuthor()); // Người tạo cũng là người thi lần đầu
        history.setExam(exam);
        history.setScore(0); // Mặc định
        history.setTimeTaken(0); // Mặc định
        history.setPassed(false); // Mặc định
        history.setCompletedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    public Page<HistoryDTO> getUserHistory(User user, PageRequest pageRequest) {
        // Logic hiện tại giữ nguyên
        Page<History> historyPage = historyRepository.findByUserOrderByCompletedAtDesc(user, pageRequest);
        return historyPage.map(history -> {
            HistoryDTO dto = new HistoryDTO();
            dto.setId(history.getId());
            dto.setExamName(history.getExam().getTitle());
            dto.setCompletedAt(history.getCompletedAt());
            dto.setTimeTaken(history.getTimeTaken());
            dto.setScore(history.getScore());
            dto.setAttempts(historyRepository.countAttemptsByUserIdAndExamId(user.getId(), history.getExam().getId()));
            dto.setPassed(history.isPassed());
            return dto;
        });
    }
}