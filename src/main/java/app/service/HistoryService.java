package app.service;

import app.dto.history.*;
import app.entity.*;
import app.exception.NotFoundException;
import app.mapper.HistoryMapper;
import app.repository.*;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;
    private final MessageHelper messageHelper;
    private final ExamService examService;
    private final UserService userService;
    private final RoomService roomService;
    private final QuestionService questionService;
    private final UserChoiceRepository userChoiceRepository;

    public Long addHistory(AddHistoryRequest request) {
        User user = userService.findInAuth();
        Exam exam;
        Room room = null;

        if (request.getRoomCode() != null && !request.getRoomCode().isBlank()) {
            room = roomService.findByCode(request.getRoomCode());
            exam = room.getExam();
        } else {
            exam = examService.findById(request.getExamId());
        }

        History history = new History();
        history.setUser(user);
        history.setExam(exam);
        history.setRoom(room);
        history.setTimeTaken(request.getTimeTaken());
        String raw = request.getFinishedAt();
        Instant utcInstant = Instant.parse(raw + "Z");
        LocalDateTime localDateTime = LocalDateTime.ofInstant(utcInstant, ZoneId.of("Asia/Ho_Chi_Minh"));
        history.setFinishedAt(localDateTime);
        List<UserChoice> userChoices = request.getChoices().stream().map(submitted -> {
            Question question = questionService.findById(submitted.getQuestionId());
            UserChoice choice = new UserChoice();
            choice.setHistory(history);
            choice.setQuestion(question);
            choice.setSelectedAnswerIds(submitted.getAnswerIds());
            return choice;
        }).toList();

        history.setUserChoices(userChoices);
        history.calculateScore();
        exam.setPlayedTimes(exam.getPlayedTimes() + 1);
        historyRepository.save(history);
        userChoiceRepository.saveAll(userChoices);
        return history.getId();
    }

    public Page<MyHistoryResponse> getAllMy(int page, int size) {
        User foundUser = userService.findInAuth();

        // Lấy tất cả lịch sử thi của người dùng theo thời gian tăng dần
        List<History> allHistories = historyRepository.findByUserIdOrderByFinishedAtAsc(foundUser.getId());

        // Map để đánh số lượt thi theo từng examId
        Map<Long, Integer> examAttemptCounter = new HashMap<>();
        List<MyHistoryResponse> allResponses = new ArrayList<>();

        for (History history : allHistories) {
            Long examId = history.getExam().getId();
            int attemptOrder = examAttemptCounter.getOrDefault(examId, 0) + 1;
            examAttemptCounter.put(examId, attemptOrder);

            MyHistoryResponse response = HistoryMapper.toMyHistoryResponse(history, attemptOrder);
            allResponses.add(response);
        }

        // Sắp xếp theo thời gian giảm dần để hiển thị bài mới nhất trước
        allResponses.sort(Comparator.comparing(MyHistoryResponse::getFinishedAt).reversed());

        // Phân trang thủ công
        int start = page * size;
        int end = Math.min(start + size, allResponses.size());
        List<MyHistoryResponse> paginated = allResponses.subList(start, end);

        return new PageImpl<>(paginated, PageRequest.of(page, size), allResponses.size());
    }

    public Room getRoomByHistoryId(long id) {
        Optional<History> history = historyRepository.findById(id);
        if (history.isEmpty()) {
            throw new NotFoundException(messageHelper.get("history.not.found"));
        }
        return history.get().getRoom();
    }

    public Page<MyCreatedHistoryResponse> getAllCreateByMe(int page, int size) {
        User foundUser = userService.findInAuth();
        List<History> allHistories = historyRepository.findHistoriesByRoom_HostOrderByFinishedAtAsc(foundUser);

        // Nhóm theo examId
        Map<Long, Integer> examAttemptCounter = new HashMap<>();
        List<MyCreatedHistoryResponse> allResponses = new ArrayList<>();

        for (History history : allHistories) {
            Long examId = history.getExam().getId();
            int attemptOrder = examAttemptCounter.getOrDefault(examId, 0) + 1;
            examAttemptCounter.put(examId, attemptOrder);

            MyCreatedHistoryResponse response = new MyCreatedHistoryResponse();
            response.setHistoryId(history.getId());
            response.setExamTitle(history.getExam().getTitle());
            response.setFinishedAt(history.getFinishedAt());
            response.setTimeTaken(history.getTimeTaken());
            response.setCountMembers(history.getRoom().getCandidates().size());
            response.setAttemptTime(attemptOrder);

            allResponses.add(response);
        }

        // Sắp xếp thời gian giảm dần để giao diện hiển thị bài mới trước
        allResponses.sort(Comparator.comparing(MyCreatedHistoryResponse::getFinishedAt).reversed());

        // Bắt đầu phân trang
        int start = page * size;
        int end = Math.min(start + size, allResponses.size());
        List<MyCreatedHistoryResponse> paginated = allResponses.subList(start, end);

        return new PageImpl<>(paginated, PageRequest.of(page, size), allResponses.size());
    }

    public HistoryDetailResponse getDetailById(Long id) {
        History history = findById(id);
        User user = history.getUser();
        Exam exam = history.getExam();

        List<UserChoice> userChoices = history.getUserChoices();
        List<Question> questions = exam.getQuestions();

        int correct = 0;
        int wrong = 0;

        List<HistoryDetailResponse.ChoiceResult> choiceResults = new ArrayList<>();
        for (UserChoice uc : userChoices) {
            Question question = uc.getQuestion();
            List<Long> correctAnswerIds = question.getCorrectAnswerIds();
            List<Long> selectedIds = uc.getSelectedAnswerIds();

            boolean isCorrect = selectedIds != null &&
                    new HashSet<>(selectedIds).equals(new HashSet<>(correctAnswerIds));

            if (isCorrect) correct++;
            else wrong++;

            choiceResults.add(new HistoryDetailResponse.ChoiceResult(
                    question.getId(),
                    selectedIds,
                    correctAnswerIds,
                    isCorrect
            ));
        }

        List<HistoryDetailResponse.QuestionDTO> fullQuestions = questions.stream()
                .map(q -> new HistoryDetailResponse.QuestionDTO(
                        q.getId(),
                        q.getContent(),
                        q.getAnswers().stream()
                                .map(a -> new HistoryDetailResponse.AnswerDTO(
                                        a.getId(),
                                        a.getContent(),
                                        a.getCorrect()
                                ))
                                .toList()
                ))
                .toList();
        int rank = calculateRankInRoom(history);
        return new HistoryDetailResponse(
                user.getUsername(),
                user.getAvatar(),
                rank,
                correct,
                wrong,
                history.getTimeTaken(),
                history.getScore(),
                choiceResults,
                fullQuestions
        );
    }

    private int calculateRankInRoom(History targetHistory) {
        Room room = targetHistory.getRoom();
        if (room == null) return -1;

        List<History> histories = historyRepository.findByRoomOrderByScoreDesc(room);
        for (int i = 0; i < histories.size(); i++) {
            if (histories.get(i).getId().equals(targetHistory.getId())) {
                return i + 1;
            }
        }
        return -1;
    }

    public History findById(Long id) {
        return historyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("history.not.found")));
    }

    public List<ExamSummaryHistoryResponse> getSummaryByExamId(Long examId) {
        Exam exam = examService.findById(examId);
        List<History> histories = historyRepository.findByExamOrderByUserIdAscFinishedAtAsc(exam);

        List<ExamSummaryHistoryResponse> responses = new ArrayList<>();
        Map<Long, Long> userAttemptCount = new HashMap<>(); // userId -> current count

        for (History history : histories) {
            Long userId = history.getUser().getId();
            long attempt = userAttemptCount.getOrDefault(userId, 0L) + 1;
            userAttemptCount.put(userId, attempt);

            int totalQuestions = history.getUserChoices() != null ? history.getUserChoices().size() : 0;
            assert history.getUserChoices() != null;
            int correct = (int) history.getUserChoices().stream().filter(UserChoice::isCorrect).count();

            responses.add(new ExamSummaryHistoryResponse(
                    history.getUser().getUsername(),
                    exam.getTitle(),
                    history.getScore(),
                    correct,
                    totalQuestions,
                    history.getTimeTaken(),
                    history.getFinishedAt(),
                    attempt
            ));
        }

        return responses;
    }

    public List<Rank> getRoomRanking(String roomCode) {
        Room room = roomService.findByCode(roomCode);
        List<History> histories = historyRepository.findByRoomOrderByScoreDescTimeTakenAsc(room);

        List<Rank> ranks = new ArrayList<>();

        for (int i = 0; i < histories.size(); i++) {
            History history = histories.get(i);
            User user = history.getUser();

            ranks.add(new Rank(
                    i + 1,
                    history.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getAvatar(),
                    history.getScore(),
                    history.getTimeTaken()
            ));
        }
        return ranks;
    }
}