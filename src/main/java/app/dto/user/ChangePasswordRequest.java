package app.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    private String email;
    @NotBlank(message = "{old.password.required}")
    private String oldPassword;
    @NotBlank(message = "{new.password.required}")
    private String newPassword;
}