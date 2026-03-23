package lumi.insert.app.worker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify; 

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder; 

import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.mail.MessagingException;
import lumi.insert.app.activitycore.entity.ActivityLog;
import lumi.insert.app.activitycore.repository.ActivityLogRepository;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;
import lumi.insert.app.core.entity.nondatabase.TransactionInvoiceMail; 
import lumi.insert.app.service.implement.MailSenderServiceImpl;

@ExtendWith(MockitoExtension.class)
public class MessageConsumerTest {
    
    @Mock
    private ActivityLogRepository repository;

    @InjectMocks
    private MessageConsumer consumer;

    @Mock
    MailSenderServiceImpl mailSenderService;

    @Test
    void activitLogsHandler_shouldSave(){
        ActivityLog activityLog = ActivityLog.builder()
        .actionMessage("a message")
        .build();

        consumer.activityLogsHandler(activityLog);

        verify(repository, times(1)).save(argThat(arg -> arg.getActionMessage().equals(activityLog.getActionMessage())));
    }

    @Test
    void transactionInvoiceMailHandler_shouldSave() throws MessagingException{
        TransactionInvoiceMail request = new TransactionInvoiceMail(UuidCreator.getTimeOrderedEpochFast(), "x@gmail.com", EmployeeLogin.builder().build());
 
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(() -> SecurityContextHolder.getContext()).thenReturn(securityContext);
            consumer.transactionInvoiceMailHandler(request);
            verify(securityContext, times(1)).setAuthentication(any());
            verify(mailSenderService, times(1)).sendTransactionInvoice(request);
        } catch (Exception e) {
            throw e;
        }
        
    }
}
