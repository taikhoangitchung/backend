package app.dto.difficulty;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddDifficultyRequest {
    @NotBlank(message = "{name.required}")
    private String name;
}
