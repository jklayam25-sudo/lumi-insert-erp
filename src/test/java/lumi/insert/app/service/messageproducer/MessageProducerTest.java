package lumi.insert.app.service.messageproducer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import lumi.insert.app.activitycore.entity.ActivityLog;
import lumi.insert.app.activitycore.entity.nondatabase.ActivityLogMessage;
import lumi.insert.app.core.entity.nondatabase.EntityList;
import lumi.insert.app.core.entity.nondatabase.TransactionInvoiceMail;
import lumi.insert.app.core.entity.nondatabase.UploadStorageMessage;
import lumi.insert.app.service.implement.MessageProducerServiceImpl;

@ExtendWith(MockitoExtension.class)
public class MessageProducerTest {
    
    @InjectMocks
    MessageProducerServiceImpl messageProducerService;

    @Mock
    RabbitTemplate rabbitTemplate;

    @Test
    void sendActivityLog_shouldTriggerConvertAndSend(){
        ActivityLog activityLog = ActivityLog.builder()
        .actionMessage("a message")
        .build();

        messageProducerService.sendActivityLog(new ActivityLogMessage(activityLog)); 
        ArgumentCaptor<ActivityLog> argumentCaptor = ArgumentCaptor.forClass(ActivityLog.class);

        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), argumentCaptor.capture());
        ActivityLog value = argumentCaptor.getValue();
        assertEquals(activityLog.getActionMessage(), value.getActionMessage());
    }

    @Test
    void sendTransactionInvoiceEmail_shouldTriggerConvertAndSend(){
        TransactionInvoiceMail transactionInvoiceMail = new TransactionInvoiceMail(null, "somemail@test", null);
        messageProducerService.sendTransactionInvoiceEmail(transactionInvoiceMail); 
        ArgumentCaptor<TransactionInvoiceMail> argumentCaptor = ArgumentCaptor.forClass(TransactionInvoiceMail.class);

        verify(rabbitTemplate, times(1)).convertAndSend(eq("main-exchange"), eq("transaction-invoice-routing"), argumentCaptor.capture());
        TransactionInvoiceMail value = argumentCaptor.getValue();
        assertEquals(transactionInvoiceMail.email(), value.email());
    }

    @Test
    void sendUploadToStorage_shouldTriggerConvertAndSend(){
        UploadStorageMessage uploadStorageMessage = new UploadStorageMessage(EntityList.SUPPLY_PAYMENT, null, null, null);
        messageProducerService.sendUploadToStorage(uploadStorageMessage); 
        ArgumentCaptor<UploadStorageMessage> argumentCaptor = ArgumentCaptor.forClass(UploadStorageMessage.class);

        verify(rabbitTemplate, times(1)).convertAndSend(eq("main-exchange"), eq("upload-storage-routing"), argumentCaptor.capture());
        UploadStorageMessage value = argumentCaptor.getValue();
        assertEquals(uploadStorageMessage.entity(), value.entity());
    }
}
