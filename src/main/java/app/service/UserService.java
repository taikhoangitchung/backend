package app.service;

import app.dto.user.ChangePasswordRequest;
import app.dto.user.LoginRequest;
import app.dto.user.RecoverPasswordRequest;
import app.dto.user.RegisterRequest;
import app.entity.PasswordRecoverToken;
import app.entity.User;
import app.exception.*;
import app.mapper.RegisterMapper;
import app.repository.TokenRepository;
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
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RegisterMapper registerMapper;
    private final MessageHelper messageHelper;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;

    @Value("${upload.directory}")
    private String uploadDirectory;

    @Value("${upload.url.prefix}")
    private String urlPrefix;

    @Value("${default.avatar}")
    private String defaultAvatar;

    public List<User> findAllExceptAdminSortByCreateAt() {
        return userRepository.findByIsAdminFalseOrderByCreateAtAsc();
    }

    public List<User> searchFollowNameAndEmail(String keyName, String keyEmail) {
        return userRepository.searchFollowNameAndEmail(keyName, keyEmail);
    }

    public void blockUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
        user.setActive(false);
        userRepository.save(user);
    }

    public boolean isDuplicatePassword(RecoverPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
        return request.getPassword().equals(user.getPassword());
    }

    public void recoverPassword(RecoverPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));

        user.setPassword(request.getPassword());
        userRepository.save(user);

        PasswordRecoverToken resetToken = tokenRepository.findByToken(request.getToken());
        tokenRepository.deleteById(resetToken.getId());
    }

    public boolean isValidRecoverToken(String token) {
        try {
            System.err.println(token);
            PasswordRecoverToken resetToken = tokenRepository.findByToken(token);
            if (resetToken != null) {
                if (!LocalDateTime.now().isAfter(resetToken.getExpiryDate())) {
                    return true;
                } else {
                    tokenRepository.deleteById(resetToken.getId());
                }
            }
            return false;
        } catch (Exception e) {
            throw new ExpiredException(messageHelper.get("expired.url"));
        }
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void register(RegisterRequest registerRequest) {
        String username = registerRequest.getUsername();
        String email = registerRequest.getEmail();
        String password = registerRequest.getPassword();
        String confirmPassword = registerRequest.getConfirmPassword();

        // Kiểm tra password và confirmPassword
        if (!password.equals(confirmPassword)) {
            throw new NotMatchException(messageHelper.get("password.confirm.not.match"));
        }

        if (username == null || username.trim().isEmpty()) {
            registerRequest.setUsername(email); // Nếu không có username, sử dụng email làm username
        }

        if (existsByEmail(email)) {
            throw new DuplicateException(messageHelper.get("email.exists"));
        }

        User user = registerMapper.toEntity(registerRequest);
        user.setAvatar(defaultAvatar); // Gán avatar mặc định khi đăng ký
        user.setCreateAt(LocalDateTime.now()); // Thiết lập thời gian tạo
        userRepository.save(user);
    }

    public String login(LoginRequest loginRequest) {
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
        return jwtService.generateToken(user);
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));

        // Cập nhật username nếu có
        if (username != null && !username.trim().isEmpty()) {
            user.setUsername(username);
        }

        // Xử lý avatar
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String fileName = avatarFile.getOriginalFilename();
                if (fileName == null || fileName.trim().isEmpty()) {
                    throw new UploadException(messageHelper.get("file.name.invalid"));
                }
                Path uploadPath = Paths.get(uploadDirectory, fileName);
                if (Files.exists(uploadPath)) {
                    String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
                    String fileExt = fileName.substring(fileName.lastIndexOf('.'));
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    fileName = fileNameWithoutExt + "_" + timestamp + fileExt;
                    uploadPath = Paths.get(uploadDirectory, fileName);
                }
                Files.createDirectories(uploadPath.getParent());
                Files.copy(avatarFile.getInputStream(), uploadPath);
                user.setAvatar(urlPrefix + fileName);
            } catch (IOException e) {
                throw new UploadException(messageHelper.get("file.upload.error") + ": " + e.getMessage());
            }
        } else {
            // Giữ nguyên avatar hiện tại, không thay đổi nếu không có tệp mới
            if (user.getAvatar() == null) {
                user.setAvatar(defaultAvatar); // Gán avatar mặc định nếu chưa có
            }
        }

        userRepository.save(user);
    }

    public Map<String, Object> getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("avatar", user.getAvatar());
        response.put("createdAt", user.getCreateAt());
        response.put("active", user.isActive());
        return response;
    }

    public void unblockUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
        user.setActive(true);
        userRepository.save(user);
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(
                ()-> new NotFoundException(messageHelper.get("user.not.found"))
        );
    }
}