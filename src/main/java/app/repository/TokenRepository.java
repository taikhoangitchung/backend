package app.repository;

import app.entity.PasswordRecoverToken;
import app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<PasswordRecoverToken, Long> {
    PasswordRecoverToken findByToken(String token);
    boolean existsByUser(User user);
    PasswordRecoverToken findByUser(User user);
}
