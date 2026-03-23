package lumi.insert.app.core.entity.nondatabase;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
