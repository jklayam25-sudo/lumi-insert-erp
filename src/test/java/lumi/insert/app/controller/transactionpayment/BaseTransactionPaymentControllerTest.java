package lumi.insert.app.controller.transactionpayment;

import java.util.UUID;

import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.controller.BaseControllerTest;
import lumi.insert.app.dto.response.TransactionPaymentResponse; 
 
@WithMockUser(username = "admin", roles = "CASHIER")
public abstract class BaseTransactionPaymentControllerTest extends BaseControllerTest{
  
    TransactionPaymentResponse transactionPaymentResponse = new TransactionPaymentResponse(UUID.randomUUID(), UUID.randomUUID(), 10000L, "CLIENT", "LUMI", false);

    TransactionPaymentResponse transactionRefundResponse = new TransactionPaymentResponse(UUID.randomUUID(), UUID.randomUUID(), 10000L, "LUMI", "CLIENT", true);

}
