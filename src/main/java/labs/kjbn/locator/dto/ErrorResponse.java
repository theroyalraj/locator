package labs.kjbn.locator.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private int code;
    private String error;
    private String message;
}
