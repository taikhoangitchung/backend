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

import app.dto.history.AddHistoryRequest.*;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;
    private final MessageHelper messageHelper;
    private final ExamService examService;
    private final UserService userService;
    private final QuestionService questionService;
    private final UserChoiceRepository userChoiceRepository;

    public HistoryDetailResponse addHistory(AddHistoryRequest request) {
        Exam exam = examService.findById(request.getExamId());
        User user = userService.findInAuth();

        History history = new History();
        history.setUser(user);
        history.setExam(exam);
        history.setTimeTaken(request.getTimeTaken());
        history.setFinishedAt(LocalDateTime.parse(request.getFinishedAt()));

        List<UserChoice> userChoices = new ArrayList<>();
        List<ChoiceResult> choiceResults = new ArrayList<>();

        int correctCount = 0;

        for (SubmittedChoice submitted : request.getChoices()) {
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
            choiceResults.add(new ChoiceResult(question.getId(), selectedIds, correctIds, isCorrect));
        }

        double rawScore = ((double) correctCount / request.getChoices().size()) * 100;
        double score = BigDecimal.valueOf(rawScore).setScale(1, RoundingMode.HALF_UP).doubleValue();
        history.setScore(score);
        history.setPassed(score >= exam.getPassScore());
        historyRepository.save(history);
        userChoiceRepository.saveAll(userChoices);

        List<QuestionDTO> fullQuestions = exam.getQuestions().stream()
                .map(this::toQuestionDTO)
                .toList();

        return new HistoryDetailResponse(
                correctCount,
                request.getChoices().size() - correctCount,
                request.getTimeTaken(),
                score,
                choiceResults,
                fullQuestions
        );
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
        List<ChoiceResult> choiceResults = new ArrayList<>();
        int correct = 0;
        for (UserChoice item : userChoices) {
            Question question = item.getQuestion();
            List<Long> correctIds = getCorrectAnswerIds(question);
            List<Long> selectedIds = item.getSelectedAnswerIds() != null ? item.getSelectedAnswerIds() : List.of();
            boolean isCorrect = new HashSet<>(selectedIds).equals(new HashSet<>(correctIds));
            if (isCorrect) correct++;
            choiceResults.add(new ChoiceResult(question.getId(), selectedIds, correctIds, isCorrect));
        }

        int wrong = userChoices.size() - correct;

        List<QuestionDTO> fullQuestions = userChoices.stream()
                .map(item -> toQuestionDTO(item.getQuestion()))
                .toList();

        return new HistoryDetailResponse(
                correct,
                wrong,
                history.getTimeTaken(),
                history.getScore(),
                choiceResults,
                fullQuestions
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
}
