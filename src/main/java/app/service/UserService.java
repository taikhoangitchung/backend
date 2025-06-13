package app.service;

import app.dto.ChangePasswordRequest;
import app.dto.LoginRequest;
import app.dto.RegisterRequest;
import app.entity.User;
import app.exception.*;
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

import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RegisterMapper registerMapper;
    private final MessageHelper messageHelper;

    @Value("${upload.directory}")
    private String uploadDirectory;

    @Value("${upload.url.prefix}")
    private String urlPrefix;

    @Value("${default.avatar")
    private String defaultAvatar;

    public List<User> findAllExceptAdminSortByCreateAt() {
        List<User> users = userRepository.findAllExceptAdmin();
        users.sort(Comparator.comparing(User::getLastLogin).reversed());
        return users;
    }

    public List<User> searchFollowNameAndEmail(String keyName, String keyEmail) {
        return userRepository.searchFollowNameAndEmail(keyName, keyEmail);
    }

    public boolean removeUser(long userId) {
        userRepository.deleteById(userId);
        return !userRepository.existsById(userId);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);

    }

    public void register(RegisterRequest registerRequest) {
        String username = registerRequest.getUsername();
        String email = registerRequest.getEmail();
        if (username == null || username.trim().isEmpty()) {
            registerRequest.setUsername(email); // Nếu không có username, sử dụng email làm username
        }

        if (existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateException(messageHelper.get("email.exist"));
        }
        User user = registerMapper.toEntity(registerRequest);
        user.setAvatar(defaultAvatar); // Gán avatar mặc định khi đăng ký
        user.setCreateAt(LocalDateTime.now()); // Thiết lập thời gian tạo
        userRepository.save(user);
    }

    public void login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new AuthException(messageHelper.get("email.not.exist"));
        }

        User user = userOptional.get();
        if (!password.equals(user.getPassword())) {
            throw new AuthException(messageHelper.get("password.incorrect"));
        }

        user.setLastLogin(LocalDateTime.now()); // Cập nhật thời gian đăng nhập
        userRepository.save(user); // Lưu lại thông tin người dùng
        // Thêm logic để tạo token hoặc session nếu cần thiết
    }

    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        User user = userRepository.findByEmail(changePasswordRequest.getEmail())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));

        if (!changePasswordRequest.getOldPassword().equals(user.getPassword())) {
            throw new NotMatchException(messageHelper.get("password.old.not.match"));
        }

        if (changePasswordRequest.getNewPassword().equals(user.getPassword())) {
            throw new SameAsOldException(messageHelper.get("password.new.match.old"));
        }

        user.setPassword(changePasswordRequest.getNewPassword());
        userRepository.save(user);
    }

    public void editProfile(String email, String username, MultipartFile avatarFile) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException(messageHelper.get("user.not.found")));

        if (username != null && !username.trim().isEmpty()) {
            user.setUsername(username);
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String fileName = avatarFile.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDirectory, fileName);
                Files.createDirectories(uploadPath.getParent());
                Files.copy(avatarFile.getInputStream(), uploadPath);
                user.setAvatar(urlPrefix + fileName);
                userRepository.save(user);
            } catch (IOException e) {
                throw new UploadException(messageHelper.get("file.upload.error"));
            }
        }
    }

    public Map<String, Object> getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("avatar", user.getAvatar());
        return response;
    }
}