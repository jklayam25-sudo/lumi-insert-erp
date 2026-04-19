package lumi.insert.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom RuntimeException for Database query failure.
 * <p>Case: Save a payment pictures but in the middle of flow, the transactions is removed/broken/etc.</p>
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class DatabaseInternalException extends RuntimeException{
    public DatabaseInternalException(String message){
        super(message);
    }
}
