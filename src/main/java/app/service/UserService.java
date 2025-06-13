package app.service;

import app.dto.EditProfileRequest;
import app.dto.LoginRequest;
import app.dto.LoginResponse;
import app.dto.RegisterRequest;
import app.entity.User;
import app.exception.AuthException;
import app.mapper.RegisterMapper;
import app.repository.UserRepository;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RegisterMapper registerMapper;
    private final MessageHelper messageHelper;

    private static final String AVATAR_UPLOAD_DIR = "src/main/resources/static/media/";
    private static final String DEFAULT_AVATAR = "default-avatar.png";

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }


    public List<User> findAllExceptAdminSortByCreateAt() {
        List<User> users = userRepository.findAllExceptAdmin();
        users.sort(Comparator.comparing(User::getCreateAt).reversed());
        return users;
    }

    public boolean isAdmin(long userId) {
        return userRepository.isAdmin(userId);
    }

    private boolean existedUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);

    }

    public ResponseEntity<String> register(RegisterRequest registerRequest) {
        // Xử lý validate từ BindingResult (nếu có lỗi từ @Valid)
        if (registerRequest == null) {
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ");
        }

        String username = registerRequest.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = registerRequest.getEmail(); // Fallback sang email
            registerRequest.setUsername(username);
        }

        if (existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại");
        }

        try {
            User user = registerMapper.toEntity(registerRequest);
            user.setAvatar(DEFAULT_AVATAR); // Gán avatar mặc định khi đăng ký
            user.setCreateAt(LocalDateTime.now()); // Thiết lập thời gian tạo
            userRepository.save(user);
            return ResponseEntity.ok("Đăng ký thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đăng ký không thành công: " + e.getMessage());
        }
    }

    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
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

        return ResponseEntity.ok(new LoginResponse("Đăng nhập thành công!", true));
    }

    public ResponseEntity<?> changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Người dùng không tồn tại"));

        if (!oldPassword.equals(user.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu cũ không đúng");
        }

        if (oldPassword.equals(newPassword)) {
            return ResponseEntity.badRequest().body("Mật khẩu mới không được giống mật khẩu cũ");
        }

        user.setPassword(newPassword); // Lưu mật khẩu plain text (không khuyến khích)
        userRepository.save(user);
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    public ResponseEntity<?> editProfile(String email, EditProfileRequest request, MultipartFile avatarFile) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Người dùng không tồn tại"));

        if (request.getUserName() == null || request.getUserName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tên hiển thị không được để trống");
        }

        user.setUsername(request.getUserName());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Validate file type
            String contentType = avatarFile.getContentType();
            if (!contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Chỉ chấp nhận file ảnh");
            }

            try {
                String fileName = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
                Path uploadPath = Paths.get(AVATAR_UPLOAD_DIR + fileName);
                Files.createDirectories(uploadPath.getParent());
                Files.copy(avatarFile.getInputStream(), uploadPath);
                user.setAvatar("/media/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi upload avatar: " + e.getMessage());
            }
        } else if (user.getAvatar() == null) {
            user.setAvatar("/media/" + DEFAULT_AVATAR);
        }

        userRepository.save(user);
        return ResponseEntity.ok("Cập nhật thông tin thành công");
    }
}