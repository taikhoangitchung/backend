package app.dto.room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class RoomWaitingResponse {
    private String examTitle;
    private String authorName;
    private String hostEmail;
    private List<String> candidateNames;
}
