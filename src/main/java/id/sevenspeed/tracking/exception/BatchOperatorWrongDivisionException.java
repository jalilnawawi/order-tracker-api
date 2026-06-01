package id.sevenspeed.tracking.exception;

import org.springframework.http.HttpStatus;

public class BatchOperatorWrongDivisionException extends BusinessException {

    public BatchOperatorWrongDivisionException() {
        super("BATCH_OPERATOR_WRONG_DIVISION",
                "Operator can only update progress in their own division",
                HttpStatus.FORBIDDEN);
    }
}