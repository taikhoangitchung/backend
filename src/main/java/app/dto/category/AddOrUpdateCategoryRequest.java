package app.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddOrUpdateCategoryRequest {
    @NotBlank(message = "{name.required}")
    @Pattern(regexp = "^\\p{L}[\\p{L}\\d ]*$", message = "{category.name.pattern}")
    private String name;

    private String description;
}
