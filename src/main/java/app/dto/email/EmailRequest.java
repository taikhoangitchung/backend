package app.dto.email;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequest {
    private String to;
    private String subject;
    private String html;
    private String token;
}
