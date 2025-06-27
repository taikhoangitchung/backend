package app.dto.room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RoomWaitingResponse {
    private String examTitle;
    private String authorName;
    private String hostEmail;
}
