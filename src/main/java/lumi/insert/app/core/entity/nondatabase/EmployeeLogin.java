package lumi.insert.app.core.entity.nondatabase;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Substitute for {@link  UserDetails}. 
 * <p>Represent auth informations.<br>
 * Class can be implemented by{@code NoArgsConstructor, AllArgsConstructor and Builder}. </p>
 * 
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLogin {
    
    private UUID id;

    private String username;

    private EmployeeRole role;

    private String ipAddress;
    
}
