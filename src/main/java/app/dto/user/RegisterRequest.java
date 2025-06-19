package app.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String username;

    @Pattern(regexp = ".+@.+\\..+", message = "{email.invalid}")
    private String email;

    @NotBlank(message = "{password.required}")
    @Size(min = 6, message = "{password.length}")
    private String password;

    @NotBlank(message = "{confirm.password.required}")
    private String confirmPassword;
}