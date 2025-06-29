package app.dto.question;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddQuestionFromExcel {
    private List<AddQuestionRequest> questions;
    private long userId;
}
