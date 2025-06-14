package app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddOrUpdateCategoryRequest {
    @NotBlank(message = "{name.required}")
    @Pattern(regexp = "^[A-Z].*", message = "{category.name.pattern}")
    private String name;

    @NotBlank(message = "{description.required}")
    private String description;
}
