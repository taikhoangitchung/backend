package app.service;

import app.dto.exam.CreateExamRequest;
import app.dto.exam.ExamSummaryResponse;
import app.dto.exam.PlayExamResponse;
import app.entity.*;
import app.exception.NotFoundException;
import app.repository.*;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final RoomRepository roomRepository;
    private final MessageHelper messageHelper;
    private final DifficultyRepository difficultyRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public void createOrUpdateExam(CreateExamRequest request, long examId) {
        Exam exam = null;

        if (examId != -1) {
            exam = findById(examId);
            exam.getQuestions().clear();
        }

        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
        Difficulty difficulty = difficultyRepository.findById(request.getDifficultyId())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("difficulty.not.found")));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("category.not.found")));

        List<Question> questions = questionRepository.findAllById(request.getQuestionIds());

        exam = (exam != null) ? exam : new Exam();
        exam.setTitle(request.getTitle());
        exam.setAuthor(author);
        exam.setDifficulty(difficulty);
        exam.setCategory(category);
        exam.setQuestions(questions);
        exam.setDuration(request.getDuration());
        exam.setPassScore(request.getPassScore());
        exam.setPlayedTimes(0);
        exam.setPublic(request.getIsPublic());
        examRepository.save(exam);
    }

    public Page<Exam> getAll(Pageable pageable, Long categoryId, String searchTerm, String ownerFilter) {
        // Xây dựng query động dựa trên các tham số
        if (categoryId != null && searchTerm != null && !searchTerm.isEmpty() && ownerFilter != null) {
            Long currentUserId = getCurrentUserId(); // Giả sử có phương thức lấy ID người dùng hiện tại
            if ("mine".equals(ownerFilter)) {
                return examRepository.findByCategoryIdAndTitleContainingIgnoreCaseAndAuthorId(
                        categoryId, searchTerm, currentUserId, pageable);
            } else if ("others".equals(ownerFilter)) {
                return examRepository.findByCategoryIdAndTitleContainingIgnoreCaseAndAuthorIdNot(
                        categoryId, searchTerm, currentUserId, pageable);
            }
            return examRepository.findByCategoryIdAndTitleContainingIgnoreCase(
                    categoryId, searchTerm, pageable);
        } else if (categoryId != null && searchTerm != null && !searchTerm.isEmpty()) {
            return examRepository.findByCategoryIdAndTitleContainingIgnoreCase(
                    categoryId, searchTerm, pageable);
        } else if (categoryId != null && ownerFilter != null) {
            Long currentUserId = getCurrentUserId();
            if ("mine".equals(ownerFilter)) {
                return examRepository.findByCategoryIdAndAuthorId(
                        categoryId, currentUserId, pageable);
            } else if ("others".equals(ownerFilter)) {
                return examRepository.findByCategoryIdAndAuthorIdNot(
                        categoryId, currentUserId, pageable);
            }
            return examRepository.findByCategoryId(categoryId, pageable);
        } else if (searchTerm != null && !searchTerm.isEmpty() && ownerFilter != null) {
            Long currentUserId = getCurrentUserId();
            if ("mine".equals(ownerFilter)) {
                return examRepository.findByTitleContainingIgnoreCaseAndAuthorId(
                        searchTerm, currentUserId, pageable);
            } else if ("others".equals(ownerFilter)) {
                return examRepository.findByTitleContainingIgnoreCaseAndAuthorIdNot(
                        searchTerm, currentUserId, pageable);
            }
            return examRepository.findByTitleContainingIgnoreCase(searchTerm, pageable);
        } else if (categoryId != null) {
            return examRepository.findByCategoryId(categoryId, pageable);
        } else if (searchTerm != null && !searchTerm.isEmpty()) {
            return examRepository.findByTitleContainingIgnoreCase(searchTerm, pageable);
        } else if (ownerFilter != null) {
            Long currentUserId = getCurrentUserId();
            if ("mine".equals(ownerFilter)) {
                return examRepository.findByAuthorId(currentUserId, pageable);
            } else if ("others".equals(ownerFilter)) {
                return examRepository.findByAuthorIdNot(currentUserId, pageable);
            }
        }
        return examRepository.findAllByOrderByIdDesc(pageable);
    }

    // Giả định phương thức lấy ID người dùng hiện tại (cần triển khai tùy theo auth)
    private Long getCurrentUserId() {
        // Ví dụ: Lấy từ SecurityContext hoặc context hiện tại
        // Thay bằng logic thực tế của bạn (ví dụ: SecurityContextHolder.getContext().getAuthentication())
        return 1L; // Placeholder, cần thay bằng logic thực tế
    }

    public PlayExamResponse getToPlayById(Long id) {
        Exam exam = findById(id);
        return new PlayExamResponse(
                exam.getDuration(),
                exam.getQuestions(),
                null,
                null
        );
    }

    public List<ExamSummaryResponse> getExamsByCategory(Long categoryId) {
        List<Exam> exams = examRepository.findByCategoryId(categoryId);
        return exams.stream()
                .map(exam -> new ExamSummaryResponse(
                        exam.getId(),
                        exam.getTitle(),
                        exam.getQuestions() != null ? exam.getQuestions().size() : 0,
                        exam.getDifficulty(),
                        exam.getPlayedTimes(),
                        exam.isPublic(),
                        exam.getAuthor().getId()
                ))
                .toList();
    }

    public boolean existExam(String title) {
        return examRepository.existsByTitle(title);
    }

    public Exam findById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("exam.not.found")));
    }

    public PlayExamResponse getToPlayByRoom(String code) {
        Room foundRoom = roomRepository.findByCode(code);
        Exam exam = foundRoom.getExam();
        return new PlayExamResponse(
                exam.getDuration(),
                exam.getQuestions(),
                foundRoom.getCandidates(),
                foundRoom.getHost().getEmail()
        );
    }

    public void delete(Long id) {
        examRepository.deleteById(id);
    }
}