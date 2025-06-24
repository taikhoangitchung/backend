package app.repository;

import app.entity.Token;
import app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findByToken(String token);
    boolean existsByUser(User user);
    Token findByUser(User user);
}
