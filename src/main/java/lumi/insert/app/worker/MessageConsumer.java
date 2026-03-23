package lumi.insert.app.worker; 

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.activitycore.entity.ActivityLog;
import lumi.insert.app.activitycore.repository.ActivityLogRepository; 
import lumi.insert.app.core.entity.nondatabase.TransactionInvoiceMail;
import lumi.insert.app.service.MailSenderService;

@Component
@Slf4j
public class MessageConsumer {

    @Autowired
    ActivityLogRepository activityLogRepository;

    @Autowired
    MailSenderService mailSenderService; 

    @RabbitListener(queues = "activity-logs")
    void activityLogsHandler(ActivityLog activityLog){ 
        activityLogRepository.save(activityLog);
    }

    @RabbitListener(queues = "transaction-invoice-mail")
    void transactionInvoiceMailHandler(TransactionInvoiceMail request) throws MessagingException{ 
        try {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(request.auth(), null, null));
            mailSenderService.sendTransactionInvoice(request);    
        } catch (Exception e) {
            log.error("Failed to send, with message: " + e.getMessage());
            throw e;
        }
                
    }

}
