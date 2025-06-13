package app.mapper;

import app.dto.RegisterRequest;
import app.entity.User;
import org.springframework.stereotype.Component;

@Component
public class RegisterMapper {
    public User toEntity(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        return user;
    }
}

