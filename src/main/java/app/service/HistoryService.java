package app.service;

import app.dto.history.*;
import app.entity.*;
import app.exception.NotFoundException;
import app.repository.*;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import app.dto.history.HistoryDetailResponse.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        history.setFinishedAt(LocalDateTime.parse(request.getFinishedAt()));

        List<UserChoice> userChoices = new ArrayList<>();
        int correctCount = 0;

        for (AddHistoryRequest.SubmittedChoice submitted : request.getChoices()) {
            Question question = questionService.findById(submitted.getQuestionId());
            List<Long> selectedIds = submitted.getAnswerIds();
            List<Long> correctIds = getCorrectAnswerIds(question);

            boolean isCorrect = new HashSet<>(selectedIds).equals(new HashSet<>(correctIds));
            if (isCorrect) correctCount++;

            UserChoice userChoice = new UserChoice();
            userChoice.setHistory(history);
            userChoice.setQuestion(question);
            userChoice.setSelectedAnswerIds(selectedIds);
            userChoices.add(userChoice);
        }

        double rawScore = ((double) correctCount / request.getChoices().size()) * 100;
        double score = BigDecimal.valueOf(rawScore).setScale(1, RoundingMode.HALF_UP).doubleValue();

        history.setScore(score);
        history.setPassed(score >= exam.getPassScore());
        exam.setPlayedTimes(exam.getPlayedTimes() + 1);

        historyRepository.save(history);
        userChoiceRepository.saveAll(userChoices);
        return history.getId();
    }

    public List<HistorySummaryResponse> getAllSummary() {
        User foundUser = userService.findInAuth();
        List<History> histories = historyRepository.findByUserIdOrderByFinishedAtDesc(foundUser.getId());

        Map<Long, List<History>> grouped = histories.stream()
                .collect(Collectors.groupingBy(h -> h.getExam().getId()));

        return histories.stream().map(history -> {
            List<History> examHistories = grouped.get(history.getExam().getId());
            int attempt = (int) examHistories.stream()
                    .filter(h -> h.getFinishedAt().isBefore(history.getFinishedAt()))
                    .count() + 1;

            HistorySummaryResponse dto = new HistorySummaryResponse();
            dto.setId(history.getId());
            dto.setExamTitle(history.getExam().getTitle());
            dto.setUsername(history.getUser().getUsername());
            dto.setFinishedAt(history.getFinishedAt());
            dto.setScore(history.getScore());
            dto.setPassed(history.isPassed());
            dto.setTimeTaken(history.getTimeTaken());
            dto.setAttemptTime(attempt);
            return dto;
        }).toList();
    }

    public HistoryDetailResponse getDetailById(Long id) {
        History history = findById(id);
        List<UserChoice> userChoices = history.getUserChoices();

        List<HistoryDetailResponse.ChoiceResult> choiceResults = new ArrayList<>();
        int correct = 0;

        for (UserChoice item : userChoices) {
            Question question = item.getQuestion();
            List<Long> correctIds = getCorrectAnswerIds(question);
            List<Long> selectedIds = item.getSelectedAnswerIds() != null ? item.getSelectedAnswerIds() : List.of();
            boolean isCorrect = new HashSet<>(selectedIds).equals(new HashSet<>(correctIds));
            if (isCorrect) correct++;

            choiceResults.add(new HistoryDetailResponse.ChoiceResult(
                    question.getId(),
                    selectedIds,
                    correctIds,
                    isCorrect
            ));
        }

        int wrong = userChoices.size() - correct;

        List<HistoryDetailResponse.QuestionDTO> fullQuestions = userChoices.stream()
                .map(item -> toQuestionDTO(item.getQuestion()))
                .toList();

        RankResponse rankResponse = null;
        if (history.getRoom() != null) {
            String roomCode = history.getRoom().getCode();
            rankResponse = getUserRank(roomCode);
        }

        return new HistoryDetailResponse(
                correct,
                wrong,
                history.getTimeTaken(),
                history.getScore(),
                choiceResults,
                fullQuestions,
                rankResponse
        );
    }


    private List<Long> getCorrectAnswerIds(Question question) {
        return question.getAnswers().stream()
                .filter(Answer::getCorrect)
                .map(Answer::getId)
                .toList();
    }

    private QuestionDTO toQuestionDTO(Question question) {
        List<AnswerDTO> answers = question.getAnswers().stream()
                .map(this::toAnswerDTO)
                .toList();
        return new QuestionDTO(question.getId(), question.getContent(), answers);
    }

    private AnswerDTO toAnswerDTO(Answer answer) {
        return new AnswerDTO(answer.getId(), answer.getContent(), answer.getCorrect());
    }

    public History findById(Long id) {
        return historyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("history.not.found")));
    }

    public List<ExamSummaryHistoryResponse> getSummaryByExamId(Long examId) {
        List<History> histories = historyRepository.findByExamId(examId);
        Map<Long, List<History>> userHistoriesMap = histories.stream()
                .collect(Collectors.groupingBy(h -> h.getUser().getId()));

        Map<Long, Integer> historyAttemptOrders = new HashMap<>();
        for (Map.Entry<Long, List<History>> entry : userHistoriesMap.entrySet()) {
            List<History> userHistories = entry.getValue();
            userHistories.sort(Comparator.comparing(History::getFinishedAt));
            for (int i = 0; i < userHistories.size(); i++) {
                historyAttemptOrders.put(userHistories.get(i).getId(), i + 1);
            }
        }

        histories.sort(Comparator
                .comparingDouble(History::getScore).reversed()
                .thenComparingLong(History::getTimeTaken));

        List<ExamSummaryHistoryResponse> result = new ArrayList<>();
        for (History history : histories) {
            int correctCount = (int) history.getUserChoices().stream()
                    .filter(UserChoice::isCorrect)
                    .count();

            int totalQuestions = history.getExam().getQuestions().size();

            long attemptOrder = historyAttemptOrders.getOrDefault(history.getId(), 1);

            result.add(new ExamSummaryHistoryResponse(
                    history.getUser().getUsername(),
                    history.getExam().getTitle(),
                    history.getScore(),
                    correctCount,
                    totalQuestions,
                    history.getTimeTaken(),
                    history.getFinishedAt(),
                    attemptOrder
            ));
        }
        return result;
    }

    public RankResponse getUserRank(String roomCode) {
        List<History> histories = historyRepository.findHistoriesByRoomCode(roomCode);
        histories.sort(Comparator
                .comparingDouble(History::getScore).reversed()
                .thenComparing(History::getFinishedAt));

        List<RankResponse.Rank> rankings = new ArrayList<>();
        for (int i = 0; i < histories.size(); i++) {
            History h = histories.get(i);
            rankings.add(new RankResponse.Rank(h.getUser().getUsername(), i + 1));
        }

        return new RankResponse(histories.size(), rankings);
    }
}
