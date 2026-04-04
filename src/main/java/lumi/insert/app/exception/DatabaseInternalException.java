package lumi.insert.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class DatabaseInternalException extends RuntimeException{
    public DatabaseInternalException(String message){
        super(message);
    }
}
