package app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendCodeRequest {
    private String to;
    private String subject;
    private String html;
}