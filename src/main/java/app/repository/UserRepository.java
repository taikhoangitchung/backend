package app.repository;

import app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByIsAdminFalseOrderByCreateAtAsc();

    @Query("SELECT u FROM User u WHERE " +
            "u.isAdmin = false AND " +
            "(:keyName IS NULL OR u.username LIKE CONCAT('%', :keyName, '%')) AND " +
            "(:keyEmail IS NULL OR u.email LIKE CONCAT('%', :keyEmail, '%'))")
    List<User> searchFollowNameAndEmail(@Param("keyName") String keyName,
                                        @Param("keyEmail") String keyEmail);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

}