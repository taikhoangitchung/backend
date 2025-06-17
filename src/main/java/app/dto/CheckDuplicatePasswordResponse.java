package app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckDuplicatePasswordResponse {
    private boolean isDuplicate;
    private String message;
}
