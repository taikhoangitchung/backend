package app.repository;

import app.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCategoryId(Long categoryId);

    boolean existsByTitle(String title);

    void deleteAllQuestionsById(long id);

    Page<Exam> findAllByOrderByIdDesc(Pageable pageable); // Thay List bằng Page
}