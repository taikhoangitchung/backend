package app.controller;

import app.dto.ChangePasswordRequest;
import app.dto.LoginRequest;
import app.dto.RegisterRequest;
import app.service.UserService;
import app.util.BindingHandler;
import app.util.MessageHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final MessageHelper messageHelper;

    @PostMapping("/register")
    public ResponseEntity<?> processRegister(@Valid @RequestBody RegisterRequest registerRequest,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(BindingHandler.getErrorMessages(bindingResult));
        }
        userService.register(registerRequest);
        return ResponseEntity.ok(messageHelper.get("register.success"));
    }

    @PatchMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        userService.login(loginRequest);
        return ResponseEntity.ok(messageHelper.get("login.success"));
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.findAllExceptAdminSortByCreateAt());
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required = false) String keyName,
                                    @RequestParam(required = false) String keyEmail) {
        return ResponseEntity.ok(userService.searchFollowNameAndEmail(keyName, keyEmail));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> removeUser(@PathVariable long userId) {
        userService.removeUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(messageHelper.get("delete.success"));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(messageHelper.get("update.success"));
    }

    @PatchMapping("/edit")
    public ResponseEntity<?> editProfile(
            @RequestParam("email") String email,
            @RequestParam("username") String username,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile) {
        userService.editProfile(email, username, avatarFile);
        return ResponseEntity.ok(messageHelper.get("update.success"));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam("email") String email) {
        return ResponseEntity.ok().body(userService.getProfile(email));
    }
}