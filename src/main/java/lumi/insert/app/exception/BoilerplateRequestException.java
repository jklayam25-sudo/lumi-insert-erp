package lumi.insert.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom RuntimeException for Boilerplate request.
 * <p>Case: Activate a category that already active</p>
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@ResponseStatus(value = HttpStatus.NOT_IMPLEMENTED)
public class BoilerplateRequestException extends RuntimeException{
    public BoilerplateRequestException (String message){
        super(message);
    }
}
