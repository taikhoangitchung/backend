package app.controller;

import app.dto.ChangePasswordRequest;
import app.dto.EditProfileRequest;
import app.dto.LoginRequest;
import app.dto.RegisterRequest;
import app.service.UserService;
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

    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
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

    @PatchMapping("/{userId}/password")
    public ResponseEntity<?> changePassword(@PathVariable long userId, @Valid @RequestBody ChangePasswordRequest request) {
        return userService.changePassword(request);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> editProfile(@PathVariable long userId, @Valid @RequestPart EditProfileRequest request,
                                         @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        return userService.editProfile(userId, request, avatarFile);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable long userId) {
        return userService.getProfile(userId);
    }
}