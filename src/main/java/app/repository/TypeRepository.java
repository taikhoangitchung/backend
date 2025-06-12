package app.repository;

import app.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeRepository extends JpaRepository<Type, Long> {
    boolean existsByName(String typeName);
    Type findByName(String typeName);
}
