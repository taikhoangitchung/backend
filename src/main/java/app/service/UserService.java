package app.service;

import app.dto.RegisterRequest;
import app.dto.UserRequest;
import app.entity.User;
import app.exception.DuplicateException;
import app.repository.UserRepository;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
//import quiz.entity.Authority;
//import quiz.repository.AuthorityRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService  {
    private final UserRepository userRepository;
//    private final AuthorityRepository authorityRepository;
//    private final JwtService jwtService;

    private final MessageHelper messageHelper;

    public void register(RegisterRequest registerRequest) {
        if (existedUsername(registerRequest.getUsername())) {
            throw new DuplicateException(messageHelper.get("username.exists"));
        }
//        User user = registerMapper.toEntity(registerRequest);
//        Set<Authority> authorities = new HashSet<>();
//        authorities.add(authorityRepository.findByRole(Authority.Role.MEMBER));
//        user.setAuthorities(authorities);
//        userRepository.save(user);
    }

    private boolean existedUsername(String username) {
        return userRepository.existsByUsername(username);
    }

//    public String login(LoginRequest loginRequest) {
//        User foundUser = userRepository.findByUsername(loginRequest.getUsername());
//        if (foundUser == null || !foundUser.getPassword().equals(loginRequest.getPassword())) {
//            throw new AuthException(messageHelper.get("auth.fail"));
//        }
//        return jwtService.generateToken(foundUser);
//    }

//    public List<UserResponse> findAll() {
//        return userRepository.findByAuthorities_RoleNot(Authority.Role.ADMIN).stream().map(userMapper::toDTO).toList();
//    }
//
//    public List<UserResponse> findByRole(Authority.Role role) {
//        return  userRepository.findByAuthorities_Role(role).stream().map(userMapper::toDTO).toList();
//    }

//    public Map<String, Long> getStats() {
//        Map<String, Long> roleStats = new HashMap<>();
//        long totalUser = userRepository.count() - 1;
//        roleStats.put("total", totalUser);
//
//        for (Authority authority : authorityRepository.findAllByRoleNot(Authority.Role.ADMIN)) {
//            roleStats.put(authority.getRole().name(), userRepository.countByAuthorities_Role(authority.getRole()));
//        }
//        return roleStats;
//    }


//    public void update(UserRequest userRequest) {
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        User foundUser = userRepository.findByUsername(username);
//        if (emailExisted(userRequest.getEmail(), foundUser)) {
//            throw new DuplicateException(messageHelper.get("email.exists"));
//        }
//
//        if (phoneExisted(userRequest.getPhone(), foundUser)) {
//            throw new DuplicateException(messageHelper.get("phone.exists"));
//        }
//        userMapper.updateEntity(userRequest, foundUser);
//        userRepository.save(foundUser);
//    }


    boolean emailExisted(String email, User foundUser) {
        return userRepository.existsByEmailAndIdNot(email, foundUser.getId());
    }

    boolean phoneExisted(String phone, User foundUser) {
        return userRepository.existsByPhoneAndIdNot(phone, foundUser.getId());
    }

//    public void addUserAuthority(Long userId, String role) {
//        User foundUser = userRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
//        Authority authority = authorityRepository.findByRole(Authority.Role.valueOf(role));
//        if (authority == null) {
//            throw new NotFoundException(messageHelper.get("authority.not.found"));
//        }
//        foundUser.getAuthorities().add(authority);
//        userRepository.save(foundUser);
//    }
}