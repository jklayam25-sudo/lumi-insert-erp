package lumi.insert.app.service.implement;
    
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import lumi.insert.app.activitycore.entity.nondatabase.ActivityLogMessage;
import lumi.insert.app.core.entity.nondatabase.TransactionInvoiceMail;
import lumi.insert.app.core.entity.nondatabase.UploadStorageMessage;
import lumi.insert.app.service.MessageProducerService;

@Service
@Slf4j
public class MessageProducerServiceImpl implements MessageProducerService{

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void sendActivityLog(ActivityLogMessage request) { 
        log.info("Sending activity log message: {}", request);
        rabbitTemplate.convertAndSend("main-exchange", "activity-routing", request);
    }

    @Override
    public void sendTransactionInvoiceEmail(TransactionInvoiceMail request) {
        log.info("Sending transaction invoice email request: {}", request);
        rabbitTemplate.convertAndSend("main-exchange", "transaction-invoice-routing", request);
    }

    @Override
    public void sendUploadToStorage(UploadStorageMessage request) {
        log.info("Sending upload-to-storage message: {}", request);
        rabbitTemplate.convertAndSend("main-exchange", "upload-storage-routing", request);
    }

    
    
}
