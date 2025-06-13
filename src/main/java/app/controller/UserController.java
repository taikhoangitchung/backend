package app.controller;

import app.dto.RegisterRequest;
import app.dto.UserRequest;
import app.service.UserService;
import app.util.BindingHandler;
import app.util.MessageHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
//import quiz.entity.Authority;


@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final MessageHelper messageHelper;

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

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest,
                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(BindingHandler.getErrorMessages(bindingResult));
        }
        userService.register(registerRequest);
        return ResponseEntity.status(201).body(messageHelper.get("register.success"));
    }

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
//        return ResponseEntity.ok(userService.login(loginRequest));
//    }

//    @GetMapping({"", "/{role}"})
//    @PreAuthorize("hasAuthority('ADMIN')")
//    public ResponseEntity<?> getUserList(@PathVariable(required = false) String role) {
//        if (role == null || role.trim().isEmpty()) {
//            return ResponseEntity.ok(userService.findAll());
//        }
//        return ResponseEntity.ok(userService.findByRole(Authority.Role.valueOf(role.toUpperCase())));
//    }

//    @GetMapping("/stats")
//    public ResponseEntity<?> getStats() {
//        return ResponseEntity.ok(userService.getStats());
//    }

    @PatchMapping
    public ResponseEntity<?> update(@Valid @RequestBody UserRequest userRequest,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(BindingHandler.getErrorMessages(bindingResult));
        }
//        userService.update(userRequest);
        return ResponseEntity.ok(messageHelper.get("update.success"));
    }

//    @PatchMapping("/{id}/authorities/add")
//    @PreAuthorize("hasAuthority('ADMIN')")
//    public ResponseEntity<?> addAuthority(@PathVariable Long id, @RequestBody String role) {
//        userService.addUserAuthority(id, role);
//        return ResponseEntity.ok(messageHelper.get("authority.add.success"));
//    }
}
