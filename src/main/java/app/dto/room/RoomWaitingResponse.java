package app.dto.room;

import app.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class RoomWaitingResponse {
    private String roomCode;
    private String examTitle;
    private String authorName;
    private Room.Status status;
    private String hostName;
    private List<String> candidateNames;
}
