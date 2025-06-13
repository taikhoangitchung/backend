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
import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class UserService {

    public final UserRepository userRepository;
    private final RegisterMapper registerMapper;
    private final MessageHelper messageHelper;

    private static final String AVATAR_UPLOAD_DIR = "src/main/resources/static/media/";
    private static final String BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_AVATAR = "default-avatar.png";

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

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
            username = registerRequest.getEmail();
            registerRequest.setUsername(username);
        }

        if (existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại. Vui lòng sử dụng email khác.");
        }

        try {
            User user = registerMapper.toEntity(registerRequest);
            user.setAvatar(DEFAULT_AVATAR); // Gán avatar mặc định khi đăng ký
            user.setCreateAt(LocalDateTime.now()); // Thiết lập thời gian tạo
            userRepository.save(user);
            return ResponseEntity.ok("Đăng ký thành công!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Dữ liệu không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Giữ log lỗi để debug
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đăng ký không thành công do lỗi hệ thống. Vui lòng thử lại.");
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

       if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
           user.setUsername(request.getUsername());
       }

       if (avatarFile != null && !avatarFile.isEmpty()) {
           try {
               String fileName = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
               Path uploadPath = Paths.get(AVATAR_UPLOAD_DIR + fileName);
               Files.createDirectories(uploadPath.getParent());
               Files.copy(avatarFile.getInputStream(), uploadPath);
               user.setAvatar(fileName); // Lưu tên file chính xác
               System.out.println("Uploaded file: " + fileName); // Debug: Kiểm tra tên file
           } catch (IOException e) {
               return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi upload avatar: " + e.getMessage());
           }
       } else if (user.getAvatar() == null) {
           user.setAvatar(DEFAULT_AVATAR); // Gán mặc định nếu chưa có avatar
       }

       userRepository.save(user);

       Map<String, String> response = new HashMap<>();
       response.put("message", "Cập nhật thông tin thành công");
       response.put("avatar", user.getAvatar() != null ? BASE_URL + "/media/" + user.getAvatar() : BASE_URL + "/media/" + DEFAULT_AVATAR);
       return ResponseEntity.ok(response);
   }

   public ResponseEntity<?> getProfile(String email) {
       User user = userRepository.findByEmail(email)
               .orElseThrow(() -> new UsernameNotFoundException("Người dùng không tồn tại"));
       Map<String, Object> response = new HashMap<>();
       response.put("username", user.getUsername() != null ? user.getUsername() : "");
       response.put("avatar", user.getAvatar() != null ? BASE_URL + "/media/" + user.getAvatar() : BASE_URL + "/media/" + DEFAULT_AVATAR);
       return ResponseEntity.ok(response);
   }
}