package lumi.insert.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INSUFFICIENT_STORAGE)
public class StorageActionException extends RuntimeException{
    public StorageActionException(String message){
        super(message);
    }
}
