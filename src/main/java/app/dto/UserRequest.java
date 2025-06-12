package app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserRequest {
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "{password.invalid}")
    private String password;

    @NotBlank(message = "{fullName.required}")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "{fullName.invalid}")
    private String fullName;

    @NotBlank(message = "{email.required}")
    @Email(message = "{email.invalid}")
    private String email;

    @Pattern(regexp = "^0\\d{9,10}$", message = "{phone.invalid}")
    private String phone;
}
