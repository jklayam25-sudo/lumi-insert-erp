package lumi.insert.app.controller.employee;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.controller.BaseControllerTest;
import lumi.insert.app.core.entity.nondatabase.EmployeeRole;
import lumi.insert.app.dto.response.EmployeeResponse; 

@WithMockUser(username = "admin", roles = "OWNER")
public abstract class BaseEmployeeControllerTest extends BaseControllerTest {
      
    EmployeeResponse employeeResponse = new EmployeeResponse(UUID.randomUUID(), "employeeU", "employeeF", EmployeeRole.CASHIER, LocalDateTime.now());
 
}
