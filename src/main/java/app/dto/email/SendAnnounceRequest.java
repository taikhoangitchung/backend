package app.dto.email;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SendAnnounceRequest {
    private String to;
    private String subject;
    private String html;
}
