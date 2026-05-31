package id.sevenspeed.tracking.exception;

import org.springframework.http.HttpStatus;

public class BatchInvalidTransitionException extends BusinessException {

    public BatchInvalidTransitionException(String message) {
        super("BATCH_INVALID_TRANSITION", message, HttpStatus.CONFLICT);
    }
}