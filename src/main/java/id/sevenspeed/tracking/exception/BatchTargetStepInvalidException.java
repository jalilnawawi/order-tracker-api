package id.sevenspeed.tracking.exception;

import org.springframework.http.HttpStatus;

public class BatchTargetStepInvalidException extends BusinessException {

    public BatchTargetStepInvalidException() {
        super("BATCH_TARGET_STEP_INVALID",
                "Rework target step sequence must be less than current step",
                HttpStatus.UNPROCESSABLE_ENTITY);
    }
}