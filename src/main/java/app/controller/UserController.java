package app.controller;

import app.dto.*;
import app.service.UserService;
import app.util.BindingHandler;
import app.util.MessageHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
        return ResponseEntity.ok().body(userService.login(loginRequest));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.findAllExceptAdminSortByCreateAt());
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required = false) String keyName,
                                    @RequestParam(required = false) String keyEmail) {
        return ResponseEntity.ok(userService.searchFollowNameAndEmail(keyName, keyEmail));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/{userId}/block")
    public ResponseEntity<?> blockUser(@PathVariable long userId) {
        userService.blockUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(messageHelper.get("block.user.success"));

    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/{userId}/unblock")
    public ResponseEntity<?> unblockUser(@PathVariable long userId) {
        userService.unblockUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(messageHelper.get("unblock.user.success"));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(messageHelper.get("update.success"));
    }

    @PatchMapping("/edit")
    public ResponseEntity<?> editProfile(
            @RequestParam("email") String email,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile) {
        userService.editProfile(email, username, avatarFile);
        return ResponseEntity.ok(messageHelper.get("update.success"));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam("email") String email) {
        return ResponseEntity.ok().body(userService.getProfile(email));
    }

    @GetMapping("/check-token/{token}")
    public ResponseEntity<?> toResetPassword(@PathVariable String token) {
        if (userService.isValidResetToken(token)) return ResponseEntity.ok(true);
        else return ResponseEntity.ok().body(messageHelper.get("expired.url"));
    }

    @PatchMapping("/recover-password")
    public ResponseEntity<?> recoverPassword(@RequestBody ResetPasswordRequest request) {
        userService.recoverPassword(request.getEmail(), request.getPassword(), request.getToken());
        return ResponseEntity.ok().body(messageHelper.get("reset.password.success"));
    }

    @PatchMapping("/check-duplicate")
    public ResponseEntity<?> checkDuplicatePassword(@RequestBody ResetPasswordRequest request) {;
        boolean isDuplicate = userService.isDuplicatePassword(request.getEmail(), request.getPassword());
        CheckDuplicatePasswordResponse duplicateResponse = new CheckDuplicatePasswordResponse();
        duplicateResponse.setDuplicate(isDuplicate);
        duplicateResponse.setMessage(isDuplicate ? messageHelper.get("password.duplicate") : "");
        return ResponseEntity.ok().body(duplicateResponse);
    }
}