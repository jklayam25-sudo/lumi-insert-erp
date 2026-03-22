package lumi.insert.app.controller.transaction;

import java.util.UUID;

import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.controller.BaseControllerTest;
import lumi.insert.app.dto.response.TransactionResponse; 
 
@WithMockUser(username = "admin", roles = "CASHIER")
public abstract class BaseTransactionControllerTest extends BaseControllerTest{
  
    public TransactionResponse transactionResponse = new TransactionResponse(UUID.randomUUID(), "INVOICE", UUID.randomUUID(), null, 1L, null, null, null, null, null, null, null, null, null, null, null, null, null);

}
