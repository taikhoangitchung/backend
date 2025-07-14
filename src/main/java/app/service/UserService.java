package app.service;

import app.dto.profile.EditProfileRequest;
import app.dto.user.*;
import app.entity.Token;
import app.entity.User;
import app.exception.*;
import app.mapper.RegisterMapper;
import app.repository.TokenRepository;
import app.repository.UserRepository;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder; // Inject PasswordEncoder

    @Value("${APP_UPLOAD_DIR}")
    private String uploadDirectory;

    @Value("${UPLOAD_URL_PREFIX}")
    private String urlPrefix;

    @Value("${default.avatar}")
    private String defaultAvatar;

    public void confirmEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("user.not.found"));
        user.setActive(true);
        userRepository.save(user);

        Token resetToken = tokenRepository.findByUser(user);
        tokenRepository.deleteById(resetToken.getId());
    }

    public List<User> findAllExceptAdminSortByCreateAt() {
        return userRepository.findByIsAdminFalseOrderByCreateAtAsc();
    }

    public List<User> searchFollowNameAndEmail(String keyName, String keyEmail) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyName, keyEmail);
    }

    public String blockUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
        user.setActive(false);
        userRepository.save(user);

        return user.getEmail();
    }

    public boolean isDuplicatePassword(RecoverPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
        return passwordEncoder.matches(request.getPassword(), user.getPassword());
    }

    public void recoverPassword(RecoverPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        deleteToken(request.getToken());
    }

    public void deleteToken(String token) {
        Token resetToken = tokenRepository.findByToken(token);
        tokenRepository.deleteById(resetToken.getId());
    }

    public boolean isValidToken(String token) {
        try {
            Token resetToken = tokenRepository.findByToken(token);
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

    public String register(RegisterRequest registerRequest) {
        String username = registerRequest.getUsername();
        String email = registerRequest.getEmail();
        String password = registerRequest.getPassword();
        String confirmPassword = registerRequest.getConfirmPassword();

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
        user.setPassword(passwordEncoder.encode(password)); // Hash password
        user.setActive(false);
        user.setAvatar(defaultAvatar); // Gán avatar mặc định khi đăng ký
        user.setCreateAt(LocalDateTime.now()); // Thiết lập thời gian tạo
        userRepository.save(user);
        return email;
    }

    public String login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new AuthException(messageHelper.get("email.not.exist"));
        }

        User user = userOptional.get();
        if (!user.isActive()) {
            throw new NotActiveException(messageHelper.get("not_active_account"));
        }

        if (!passwordEncoder.matches(password, user.getPassword())) { // Kiểm tra hash
            throw new AuthException(messageHelper.get("password.incorrect"));
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        return jwtService.generateToken(user);
    }

    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        User user = userRepository.findByEmail(changePasswordRequest.getEmail())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new NotMatchException(messageHelper.get("password.old.not.match"));
        }

        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new SameAsOldException(messageHelper.get("password.new.match.old"));
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
    }

    public void editProfile(EditProfileRequest request, MultipartFile avatar) throws IOException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
        user.setUsername(request.getUsername());

        if (avatar != null && !avatar.isEmpty()) {
            if (avatar.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException(messageHelper.get("file.size.exceeded"));
            }
            if (!avatar.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException(messageHelper.get("invalid.file.type"));
            }
            String fileName = System.currentTimeMillis() + "_" + avatar.getOriginalFilename();
            Path filePath = Paths.get(uploadDirectory, fileName);
            Files.createDirectories(filePath.getParent());
            avatar.transferTo(filePath.toFile());
            user.setAvatar(urlPrefix + fileName);
        } else {
            user.setAvatar(request.getAvatar() != null ? request.getAvatar() : defaultAvatar);
        }
        userRepository.save(user);
    }

    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(Optional.ofNullable(user.getAvatar()).orElse(defaultAvatar))
                .role(user.isAdmin() ? "ADMIN" : "USER")
                .active(user.isActive())
                .isAdmin(user.isAdmin())
                .createdAt(user.getCreateAt())
                .googleId(user.getGoogleId())
                .build();
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

    public User findInAuth(){
        return findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    public void registerOrUpdateGoogleUser(String email, String username, String googleId) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            // Nếu user đã có googleId, kiểm tra và cập nhật nếu khác
            if (existingUser.getGoogleId() != null && !existingUser.getGoogleId().equals(googleId)) {
                throw new DuplicateException(messageHelper.get("google.account.linked.different"));
            }
            // Cập nhật googleId nếu chưa có hoặc giữ nguyên nếu đã có
            if (existingUser.getGoogleId() == null) {
                existingUser.setGoogleId(googleId);
                userRepository.save(existingUser);
            }
        } else {
            // Tạo user mới nếu email chưa tồn tại
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(username != null ? username : email);
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Hash mật khẩu giả
            newUser.setGoogleId(googleId);
            newUser.setActive(true);
            newUser.setCreateAt(LocalDateTime.now());
            newUser.setAvatar(defaultAvatar);
            userRepository.save(newUser);
        }
    }

    public Map<String, String> refreshToken(String refreshToken) {
        Map<String, String> response = new HashMap<>();
        try {
            String newAccessToken = tokenService.refreshAccessToken(refreshToken);
            response.put("accessToken", newAccessToken);
        } catch (Exception e) {
            response.put("error", messageHelper.get("refresh.token.invalid"));
        }
        return response;
    }
}