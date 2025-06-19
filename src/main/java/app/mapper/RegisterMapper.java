package app.mapper;

import app.dto.user.RegisterRequest;
import app.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RegisterMapper {
    public User toEntity(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setCreateAt(LocalDateTime.now()); // Thiết lập thời gian tạo
        return user;
    }
}

