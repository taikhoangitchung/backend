package app.repository;

import app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
//import quiz.entity.Authority;

import java.util.List;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.active = false")
    List<User> findAllExceptAdmin();

    @Query("SELECT u.active FROM User u WHERE u.id = :userId")
    boolean isAdmin(@Param("userId") long userId);

    @Query("SELECT u FROM User u WHERE ((:keyName IS NULL OR u.username LIKE %:keyName%) " +
            "AND (:keyEmail IS NULL OR u.email LIKE %:keyEmail%))")
    List<User> searchFollowNameAndEmail(@Param("keyName") String keyName,@Param("keyEmail") String keyEmail);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    User findByUsername(String username);

    boolean existsByUsername(String username);

//    List<User> findByActiveTrue();

//    List<User> findByAuthorities_Role(Authority.Role role);
//
//    long countByAuthorities_Role(Authority.Role role);
//
//    List<User> findByAuthorities_RoleNot(Authority.Role role);
}