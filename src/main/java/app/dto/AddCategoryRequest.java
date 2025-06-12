package app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddCategoryRequest {
    @NotBlank(message = "{category.name.required}")
    private String name;
}
