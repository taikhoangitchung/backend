package app.dto.room;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StartRoomRequest {
    @Positive(message = "{duration.invalid}")
    private long duration;
}
