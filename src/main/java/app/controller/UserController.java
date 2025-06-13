package app.controller;

import app.dto.ChangePasswordRequest;
import app.dto.EditProfileRequest;
import app.dto.LoginRequest;
import app.dto.RegisterRequest;
import app.service.UserService;
import app.util.MessageHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.findAllExceptAdminSortByCreateAt());
    }

    @GetMapping("/is-admin/{userId}")
    public ResponseEntity<?> isAdmin(@PathVariable long userId) {
        return ResponseEntity.ok(userService.isAdmin(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required = false) String keyName,
                                                      @RequestParam(required = false) String keyEmail) {
        return ResponseEntity.ok(userService.searchFollowNameAndEmail(keyName, keyEmail));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> removeUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.removeUser(userId));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        return userService.changePassword(request.getEmail(), request.getOldPassword(), request.getNewPassword());
    }

    @PostMapping("/edit")
    public ResponseEntity<?> editProfile(
            @RequestParam("email") String email,
            @RequestParam("username") String username,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile) {
        EditProfileRequest request = new EditProfileRequest();
        request.setUsername(username);
        return userService.editProfile(email, request, avatarFile);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam("email") String email) {
        return userService.getProfile(email);
    }
}