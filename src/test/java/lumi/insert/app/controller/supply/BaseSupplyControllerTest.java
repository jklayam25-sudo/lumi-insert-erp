package lumi.insert.app.controller.supply; 

import org.springframework.security.test.context.support.WithMockUser;

import com.github.f4b6a3.uuid.UuidCreator;
 
import lumi.insert.app.controller.BaseControllerTest;
import lumi.insert.app.dto.response.SupplyResponse; 
 
@WithMockUser(username = "admin", roles = "WAREHOUSE")
public abstract class BaseSupplyControllerTest extends BaseControllerTest{
     
    public SupplyResponse supplyResponse = new SupplyResponse(UuidCreator.getTimeOrderedEpochFast(), "INV", UuidCreator.getTimeOrderedEpochFast(), null, null, null, null, null, null, null, null, null, null, null, null, null, null);

}
