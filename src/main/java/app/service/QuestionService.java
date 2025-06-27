package app.service;

import app.dto.question.*;
import app.entity.*;
import app.exception.ExcelImportException;
import app.exception.LockedException;
import app.exception.NotFoundException;
import app.exception.UploadException;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        return questionRepository.findAll();
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
                String fileName = image.getOriginalFilename();
                if (fileName == null || fileName.trim().isEmpty()) {
                    throw new UploadException(messageHelper.get("file.name.invalid"));
                }
                Path uploadPath = Paths.get(uploadDirectory, fileName);
                if (Files.exists(uploadPath)) {
                    String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
                    String fileExt = fileName.substring(fileName.lastIndexOf('.'));
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    fileName = fileNameWithoutExt + "_" + timestamp + fileExt;
                    uploadPath = Paths.get(uploadDirectory, fileName);
                }
                Files.createDirectories(uploadPath.getParent());
                Files.copy(image.getInputStream(), uploadPath);
                question.setImage(urlPrefix + fileName);
            } catch (IOException e) {
                throw new UploadException(messageHelper.get("file.upload.error") + ": " + e.getMessage());
            }
        }

        questionRepository.save(question);
    }

    @Transactional
    public void addAllQuestionFromExcel(MultipartFile file, long userId) {
        Map<Long, Question> questionMap = new LinkedHashMap<>();

        if (file.isEmpty()) {
            throw new NotFoundException(messageHelper.get("file.not.found"));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));

        try (InputStream fis = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null || row.getCell(1) == null) continue;

                long id = (long) row.getCell(0).getNumericCellValue();
                String content = row.getCell(1).getStringCellValue().trim();
                String categoryName = row.getCell(2).getStringCellValue().trim();
                String difficultyName = row.getCell(3).getStringCellValue().trim();
                String typeName = row.getCell(4).getStringCellValue().trim();
                String contentAnswer = row.getCell(5).getStringCellValue().trim();
                boolean correct = Boolean.parseBoolean(row.getCell(6).toString().trim());

                Category category = Optional.ofNullable(categoryRepository.findByName(categoryName))
                        .orElseGet(() -> categoryRepository.findById(0L).orElse(null));
                Difficulty difficulty = Optional.ofNullable(difficultyRepository.findByName(difficultyName))
                        .orElseGet(() -> difficultyRepository.findById(0L).orElse(null));
                Type type = typeRepository.findByName(typeName);

                if (!questionMap.containsKey(id)) {
                    Question question = new Question();
                    question.setContent(content);
                    question.setCategory(category);
                    question.setDifficulty(difficulty);
                    question.setType(type);
                    question.setUser(user);
                    question.setExams(Collections.emptyList());
                    question.setAnswers(new ArrayList<>());
                    questionMap.put(id, question);
                }

                // Thêm answer
                Answer answer = new Answer();
                answer.setContent(contentAnswer);
                answer.setCorrect(correct);
                answer.setQuestion(questionMap.get(id));
                questionMap.get(id).getAnswers().add(answer);
            }

            questionRepository.saveAll(new ArrayList<>(questionMap.values()));
        } catch (Exception e) {
            throw new ExcelImportException(e.getMessage() + " " + messageHelper.get("excel.import.error"));
        }
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
        if (!question.getExams().isEmpty()) {
            throw new LockedException(messageHelper.get("question.update.conflict"));
        }

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

        if (image != null && !image.isEmpty()) {
            try {
                String fileName = image.getOriginalFilename();
                if (fileName == null || fileName.trim().isEmpty()) {
                    throw new UploadException(messageHelper.get("file.name.invalid"));
                }
                Path uploadPath = Paths.get(uploadDirectory, fileName);
                if (Files.exists(uploadPath)) {
                    String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
                    String fileExt = fileName.substring(fileName.lastIndexOf('.'));
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    fileName = fileNameWithoutExt + "_" + timestamp + fileExt;
                    uploadPath = Paths.get(uploadDirectory, fileName);
                }
                Files.createDirectories(uploadPath.getParent());
                Files.copy(image.getInputStream(), uploadPath);
                question.setImage(urlPrefix + fileName);
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
                Path path = Paths.get(uploadDirectory, "media", question.getImage().replace(urlPrefix, "").replaceFirst("/", ""));
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
}