package app.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecoverPasswordRequest {
    @NotBlank(message = "{email.required}")
    private String email;

    @Size(min = 6, message = "Mật khẩu quá ngắn")
    private String password;

    @NotBlank(message = "{token.required}")
    private String token;
}
