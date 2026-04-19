package lumi.insert.app.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom AuthenticationException.
 * <p>Case: Credentials wrong</p>
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class AuthenticationTokenException extends AuthenticationException{
    public AuthenticationTokenException(@Nullable String message) {
        super(message); 
    }
    
}
