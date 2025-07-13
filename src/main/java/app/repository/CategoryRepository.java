package app.repository;

import app.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c WHERE (:name IS NULL OR :name = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Category> findAllWithSearch(@Param("name") String name, Pageable pageable);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(TRIM(c.name)) = LOWER(TRIM(:name))")
    boolean existsByName(@Param("name") String name);

    Category findByName(String name);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(TRIM(c.name)) = LOWER(TRIM(:name)) AND c.id <> :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
}