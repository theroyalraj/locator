package labs.kjbn.locator.exception;

import labs.kjbn.locator.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleCustomException(Exception e) throws IOException {
        log.error("Exception : {}", ExceptionUtils.getStackTrace(e));
        ErrorResponse errorResponse = fromException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException : {}", ExceptionUtils.getStackTrace(e));
        ErrorResponse errorResponse = fromException(e, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    public ErrorResponse fromException(Exception ex, HttpStatus httpStatus) {
        return ErrorResponse.builder()
                .code(httpStatus.value())
                .error(httpStatus.name())
                .message(ex.getMessage())
                .build();
    }

}
