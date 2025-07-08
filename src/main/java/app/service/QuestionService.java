package app.service;

import app.dto.question.*;
import app.entity.*;
import app.exception.*;
import app.repository.*;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    @Value("${upload.directory}")
    private String uploadDirectory;

    @Value("${upload.url.prefix}")
    private String urlPrefix;

    @Value("${admin.username}")
    private String adminUsername;

    public List<Question> getAll() {
        return questionRepository.findAllByOrderByIdDesc();
    }

    @Transactional
    public void addQuestion(AddQuestionRequest request, MultipartFile image) throws IOException {
        Question question = new Question();
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User foundUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
        question.setUser(foundUser);
        question.setCategory(categoryRepository.findByName(request.getCategory()));
        question.setType(typeRepository.findByName(request.getType()));
        question.setDifficulty(difficultyRepository.findByName(request.getDifficulty()));
        question.setContent(request.getContent());
        for (Answer item : request.getAnswers()) {
            item.setQuestion(question);
        }
        question.setAnswers(request.getAnswers());

        if (image != null && !image.isEmpty()) {
            try {
                String originalName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
                if (originalName.isBlank()) {
                    throw new UploadException(messageHelper.get("file.name.invalid"));
                }

                String contentType = image.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new UploadException(messageHelper.get("image.type.invalid"));
                }
                String extension = originalName.substring(originalName.lastIndexOf("."));
                String baseName = originalName.substring(0, originalName.lastIndexOf(".")).replaceAll("[^a-zA-Z0-9_-]", "");
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String safeFileName = baseName + "_" + timestamp + extension;

                Path uploadPath = Paths.get(uploadDirectory);
                Files.createDirectories(uploadPath);

                Path destination = uploadPath.resolve(safeFileName);
                Files.copy(image.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
                question.setImage(urlPrefix + safeFileName);
            } catch (IOException e) {
                throw new UploadException(messageHelper.get("file.upload.error") + ": " + e.getMessage());
            }
        }
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
    public void update(EditQuestionRequest request, long id, MultipartFile image) throws IOException {
        Question question = findById(id);
        Category category = categoryRepository.findByName(request.getCategory());
        if (category == null) {
            throw new NotFoundException(messageHelper.get("category.not.found"));
        }
        Type type = typeRepository.findByName(request.getType());
        if (type == null) {
            throw new NotFoundException(messageHelper.get("type.not.found"));
        }
        Difficulty difficulty = difficultyRepository.findByName(request.getDifficulty());
        if (difficulty == null) {
            throw new NotFoundException(messageHelper.get("difficulty.not.found"));
        }

        answerRepository.deleteAllByQuestionId(id);

        question.setCategory(category);
        question.setType(type);
        question.setDifficulty(difficulty);
        question.setContent(request.getContent());

        question.getAnswers().clear();
        for (Answer item : request.getAnswers()) {
            item.setQuestion(question);
            question.getAnswers().add(item);
        }

        if (image == null) {
            if (question.getImage() != null) {
                try {
                    String oldImage = question.getImage().replace(urlPrefix, "").replaceFirst("^/", "");
                    Path oldPath = Paths.get(uploadDirectory, oldImage);
                    Files.deleteIfExists(oldPath);
                } catch (IOException e) {
                    throw new UploadException("Xoá ảnh cũ thất bại: " + e.getMessage());
                }
            }
            question.setImage(null);
        } else if (!image.isEmpty()) {
            try {
                String originalName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
                if (originalName.isBlank()) {
                    throw new UploadException(messageHelper.get("file.name.invalid"));
                }

                String contentType = image.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new UploadException(messageHelper.get("image.type.invalid"));
                }
                String extension = originalName.substring(originalName.lastIndexOf("."));
                String baseName = originalName.substring(0, originalName.lastIndexOf(".")).replaceAll("[^a-zA-Z0-9_-]", "");
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String safeFileName = baseName + "_" + timestamp + extension;

                Path uploadPath = Paths.get(uploadDirectory);
                Files.createDirectories(uploadPath);

                Path destination = uploadPath.resolve(safeFileName);
                Files.copy(image.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
                question.setImage(urlPrefix + safeFileName);
            } catch (IOException e) {
                throw new UploadException(messageHelper.get("file.upload.error") + ": " + e.getMessage());
            }
        }
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

    public List<Question> findWithFilters(FilterQuestionRequest request) {
        return questionRepository.findWithFilters(
                request.getSourceId(),
                request.getCategoryId(),
                request.getCurrentUserId(),
                request.getUsername()
        );
    }

    public void ensureEditable(Question question) {
        if (!question.getExams().isEmpty()) {
            throw new LockedException(messageHelper.get("question.update.conflict"));
        }
    }
}