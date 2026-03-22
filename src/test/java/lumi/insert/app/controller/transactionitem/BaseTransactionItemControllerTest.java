package lumi.insert.app.controller.transactionitem;

import java.util.UUID;

import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.controller.BaseControllerTest;
import lumi.insert.app.dto.response.TransactionItemResponse; 
 
@WithMockUser(username = "admin", roles = "CASHIER")
public abstract class BaseTransactionItemControllerTest extends BaseControllerTest{
  
    public TransactionItemResponse transactionItemResponse = new TransactionItemResponse(UUID.randomUUID(), UUID.randomUUID(), 1L, null, null, 10L, 5L, null, null);

}