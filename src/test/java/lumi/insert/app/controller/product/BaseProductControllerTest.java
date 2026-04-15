package lumi.insert.app.controller.product;

import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.controller.BaseControllerTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@WithMockUser(username = "admin", roles = "WAREHOUSE")
public abstract class BaseProductControllerTest extends BaseControllerTest{ 
 
}
