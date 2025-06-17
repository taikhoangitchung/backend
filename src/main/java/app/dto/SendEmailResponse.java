package app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailResponse {
    private String token;
    private String message;
}
