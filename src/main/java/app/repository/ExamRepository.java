package app.repository;

import app.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCategoryId(Long categoryId);

    List<Exam> findAllByAuthorId(Long id);

    boolean existsByTitle(String title);
}
