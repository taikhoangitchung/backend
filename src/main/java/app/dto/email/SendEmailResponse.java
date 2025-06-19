package app.dto.email;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailResponse {
    private String token;
    private String message;
}
