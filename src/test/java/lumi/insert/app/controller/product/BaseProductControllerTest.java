package lumi.insert.app.controller.product;

import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.controller.BaseControllerTest;

@WithMockUser(username = "admin", roles = "WAREHOUSE")
public abstract class BaseProductControllerTest extends BaseControllerTest{ 
 
}
