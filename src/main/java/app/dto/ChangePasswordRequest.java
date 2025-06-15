package app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    private Long userId;
    @NotBlank(message = "{old.password.required}")
    private String oldPassword;
    @NotBlank(message = "{new.password.required}")
    private String newPassword;
}