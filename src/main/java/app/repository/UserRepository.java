package app.repository;

import app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByIsAdminFalseOrderByCreateAtAsc();

    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

}