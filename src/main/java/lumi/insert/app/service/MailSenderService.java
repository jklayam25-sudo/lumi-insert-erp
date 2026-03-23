package lumi.insert.app.service; 

import java.time.LocalDateTime;

import jakarta.mail.MessagingException;
import lumi.insert.app.core.entity.nondatabase.TransactionInvoiceMail;
 

public interface MailSenderService {

    void sendTransactionInvoice(TransactionInvoiceMail request) throws MessagingException;

    void sendProductsStatistic(LocalDateTime startDate, LocalDateTime endDate) throws MessagingException;

}
