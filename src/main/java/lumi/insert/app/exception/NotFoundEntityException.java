package lumi.insert.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom RuntimeException for NotFound any entity.
 * <p>Case: Update Product with ID 1 but there is no product with that id in database.</p>
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundEntityException extends RuntimeException{
    public NotFoundEntityException (String message){
        super(message);
    }
}
