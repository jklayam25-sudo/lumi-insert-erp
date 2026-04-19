package lumi.insert.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom RuntimeException for Request that doesn't meet transaction flow logic.
 * <p>Case: Cancel a pending transaction</p>
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class TransactionValidationException extends RuntimeException{
    public TransactionValidationException (String message){
        super(message);
    }
}
