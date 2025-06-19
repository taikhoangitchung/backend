package app.dto.email;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendCodeRequest {
    private String to;
    private String subject;
    private String html;
}