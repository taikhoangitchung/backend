package app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "{email.required}")
    @Email(message = "{email.invalid}")
    @Pattern(regexp = ".+@.+\\..+", message = "Email phải có domain hợp lệ (ví dụ: @gmail.com)")
    private String email;

    @NotBlank(message = "{password.required}")
    @Size(min = 6, message = "{password.min}")
    private String password;
}
