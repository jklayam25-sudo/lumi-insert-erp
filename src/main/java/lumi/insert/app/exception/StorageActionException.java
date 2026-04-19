package lumi.insert.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom RuntimeException for IO Exception at Storage flow.
 * <p>Case: Failed to upload to storage< 3rd issue, fail to save tmp file, etc</p>
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@ResponseStatus(value = HttpStatus.INSUFFICIENT_STORAGE)
public class StorageActionException extends RuntimeException{
    public StorageActionException(String message){
        super(message);
    }
}
