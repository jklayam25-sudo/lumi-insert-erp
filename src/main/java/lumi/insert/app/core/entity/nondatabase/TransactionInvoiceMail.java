package lumi.insert.app.core.entity.nondatabase;

import java.util.UUID;

import org.slf4j.MDC;

import lumi.insert.app.service.implement.MessageProducerServiceImpl; 

/**
 * Wrapper broker message for  {@link MessageProducerServiceImpl#sendTransactionInvoiceEmail(TransactionInvoiceMail)}. 
 * 
 * @author KelvinKhodes
 * @since 1.0.0 
 */
public record TransactionInvoiceMail (UUID transactionId, String email, EmployeeLogin auth, String requestId) {

    /**
     * Default constructor of this wrapper
     * <p>Assign MDC(Request ID) for trace and logging.</p>
     * @param transactionId
     * @param email
     * @param auth
     */
    public TransactionInvoiceMail (UUID transactionId, String email, EmployeeLogin auth){
        this(transactionId, email, auth, MDC.get("requestId")); 
    };
}

