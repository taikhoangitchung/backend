package app.dto.type;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddTypeRequest {
    @NotBlank(message = "{name.required}")
    private String name;
}
