package app.dto.profile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditProfileRequest {
    private String username;
    private String email;
    private String avatar;
}
