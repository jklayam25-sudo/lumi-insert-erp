package lumi.insert.app.controller.transactionitem;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.controller.BaseControllerTest;
import lumi.insert.app.dto.response.TransactionItemResponse; 
 
@WithMockUser(username = "admin", roles = "CASHIER")
public abstract class BaseTransactionItemControllerTest extends BaseControllerTest{
  
    public TransactionItemResponse transactionItemResponse = new TransactionItemResponse(UUID.randomUUID(), UUID.randomUUID(), 1L, null, null, BigDecimal.valueOf(10L), BigDecimal.valueOf(5L), null, null);

}