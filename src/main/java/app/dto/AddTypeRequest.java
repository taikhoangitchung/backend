package app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddTypeRequest {
    @NotBlank(message = "{type.name.required}")
    private String name;
}
