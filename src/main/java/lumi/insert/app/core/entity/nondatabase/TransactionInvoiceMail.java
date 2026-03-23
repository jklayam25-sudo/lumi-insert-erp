package lumi.insert.app.core.entity.nondatabase;

import java.util.UUID; 

public record TransactionInvoiceMail (UUID transactionId, String email, EmployeeLogin auth){
    
}
