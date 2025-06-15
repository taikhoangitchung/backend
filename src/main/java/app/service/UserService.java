package app.service;

import app.dto.*;
import app.entity.User;
import app.exception.AuthException;
import app.exception.DuplicateException;
import app.exception.NotFoundException;
import app.mapper.RegisterMapper;
import app.repository.UserRepository;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class UserService {

    public final UserRepository userRepository;
    private final RegisterMapper registerMapper;
    private final MessageHelper messageHelper;

    @Value("${app.upload.dir:uploads/media/}")
    private String AVATAR_UPLOAD_DIR;
    private static final String BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_AVATAR = "default-avatar.png";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_FILE_TYPES = List.of("image/jpeg", "image/png");

    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(messageHelper.get("username.required"));
        }
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(messageHelper.get("email.required"));
        }
        return userRepository.existsByEmail(email);
    }

    public List<User> findAllExceptAdminSortByCreateAt() {
        List<User> users = userRepository.findAllExceptAdmin();
        users.sort(Comparator.comparing(User::getLastLogin).reversed());
        return users;
    }

    public boolean isAdmin(long userId) {
        return userRepository.isAdmin(userId);
    }

    public List<User> searchFollowNameAndEmail(String keyName, String keyEmail) {
        return userRepository.searchFollowNameAndEmail(keyName, keyEmail);
    }

    public boolean removeUser(long userId) {
        userRepository.deleteById(userId);
        return !userRepository.existsById(userId);
    }

    public ResponseEntity<UserResponse> register(RegisterRequest registerRequest) {
        if (registerRequest == null) {
            throw new IllegalArgumentException(messageHelper.get("invalid.request.data"));
        }

        String username = registerRequest.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = registerRequest.getEmail().split("@")[0];
            registerRequest.setUsername(username);
        }

        if (existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateException(messageHelper.get("email.exists"));
        }
        if (existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateException(messageHelper.get("username.exists"));
        }

        try {
            User user = registerMapper.toEntity(registerRequest);
            user.setAvatar(DEFAULT_AVATAR);
            user.setCreateAt(LocalDateTime.now());
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(toUserResponse(user));
        } catch (Exception e) {
            throw new RuntimeException(messageHelper.get("register.failed") + ": " + e.getMessage(), e);
        }
    }

    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        if (loginRequest == null || loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            throw new IllegalArgumentException(messageHelper.get("invalid.login.data"));
        }

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("email.not.exist")));

        if (!loginRequest.getPassword().equals(user.getPassword())) {
            throw new AuthException(messageHelper.get("password.incorrect"));
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok(new LoginResponse(messageHelper.get("login.success"), true));
    }

    public ResponseEntity<String> changePassword(ChangePasswordRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));

        if (!request.getOldPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException(messageHelper.get("old.password.incorrect"));
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException(messageHelper.get("new.password.same"));
        }

        user.setPassword(request.getNewPassword());
        userRepository.save(user);
        return ResponseEntity.ok(messageHelper.get("password.change.success"));
    }

    public ResponseEntity<UserResponse> editProfile(long userId, EditProfileRequest request, MultipartFile avatarFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (!request.getUsername().equals(user.getUsername()) && existsByUsername(request.getUsername())) {
                throw new DuplicateException(messageHelper.get("username.exists"));
            }
            user.setUsername(request.getUsername());
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            if (avatarFile.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException(messageHelper.get("file.size.limit"));
            }
            if (!ALLOWED_FILE_TYPES.contains(avatarFile.getContentType())) {
                throw new IllegalArgumentException(messageHelper.get("file.type.unsupported"));
            }
            try {
                String fileName = UUID.randomUUID() + "_" + avatarFile.getOriginalFilename();
                Path uploadPath = Paths.get(AVATAR_UPLOAD_DIR, fileName);
                Files.createDirectories(uploadPath.getParent());
                Files.copy(avatarFile.getInputStream(), uploadPath);
                user.setAvatar(fileName);
            } catch (IOException e) {
                throw new RuntimeException(messageHelper.get("avatar.upload.failed") + ": " + e.getMessage(), e);
            }
        }

        userRepository.save(user);
        return ResponseEntity.ok(toUserResponse(user));
    }

    public ResponseEntity<UserResponse> getProfile(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
        return ResponseEntity.ok(toUserResponse(user));
    }

    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar() != null ? BASE_URL + "/media/" + user.getAvatar() : BASE_URL + "/media/" + DEFAULT_AVATAR);
        return response;
    }
}