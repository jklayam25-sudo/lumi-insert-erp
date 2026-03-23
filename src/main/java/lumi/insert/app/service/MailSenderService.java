package lumi.insert.app.service; 

import jakarta.mail.MessagingException;
import lumi.insert.app.core.entity.nondatabase.TransactionInvoiceMail;
 

public interface MailSenderService {

    void sendTransactionInvoice(TransactionInvoiceMail request) throws MessagingException;

}
