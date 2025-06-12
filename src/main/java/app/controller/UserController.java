package app.controller;

import app.dto.ChangePasswordRequest;
import app.dto.LoginRequest;
import app.dto.RegisterRequest;
import app.service.UserService;
import app.util.MessageHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final MessageHelper messageHelper;

    @PostMapping("/register")
    public ResponseEntity<String> processRegister(@Valid @RequestBody RegisterRequest registerRequest) {
        System.out.println("Đăng ký với: " + registerRequest);
        return userService.register(registerRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        return userService.changePassword(request.getEmail(), request.getOldPassword(), request.getNewPassword());
    }
}