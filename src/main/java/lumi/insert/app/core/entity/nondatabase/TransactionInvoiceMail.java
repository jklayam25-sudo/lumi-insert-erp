package lumi.insert.app.core.entity.nondatabase;

import java.util.UUID;

import org.slf4j.MDC; 

public record TransactionInvoiceMail (UUID transactionId, String email, EmployeeLogin auth, String requestId) {
    public TransactionInvoiceMail (UUID transactionId, String email, EmployeeLogin auth){
        this(transactionId, email, auth, MDC.get("requestId"));
    };
}
