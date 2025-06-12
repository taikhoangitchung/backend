package app.dto;

import lombok.Getter;
import lombok.Setter;
//import quiz.entity.Authority;

import java.util.Set;

@Setter
@Getter
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
//    private Set<Authority> authorities;
}
