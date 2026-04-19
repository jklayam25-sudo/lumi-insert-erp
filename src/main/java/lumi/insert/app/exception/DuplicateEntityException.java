package lumi.insert.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom RuntimeException for Duplicated entity.
 * <p>Case: Registering a same product</p>
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class DuplicateEntityException extends RuntimeException{
    public DuplicateEntityException (String message){
        super(message);
    }
}