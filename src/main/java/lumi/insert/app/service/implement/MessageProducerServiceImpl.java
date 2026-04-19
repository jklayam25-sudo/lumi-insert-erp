package lumi.insert.app.service.implement;
    
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import lumi.insert.app.activitycore.entity.nondatabase.ActivityLogMessage;
import lumi.insert.app.core.entity.nondatabase.TransactionInvoiceMail;
import lumi.insert.app.core.entity.nondatabase.UploadStorageMessage;
import lumi.insert.app.service.MessageProducerService;

/**
 * Implementation of {@link MessageProducerService} for asynchronous message dispatching.
 * <p>
 * This service acts as a producer in the system's messaging infrastructure, leveraging 
 * {@link RabbitTemplate} to push events to a RabbitMQ broker. By offloading tasks 
 * like logging, email dispatching, and heavy I/O storage operations to a message queue, 
 * it enables non-blocking execution and improves system scalability.
 * </p>
 *
 * @author KelvinKhodes
 * @since 1.0.0
 */
@Service
@Slf4j
public class MessageProducerServiceImpl implements MessageProducerService{

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * Dispatches an activity log event for audit trail processing.
     * <p>Pushes the message to {@code main-exchange} using the {@code activity-routing} key.</p>
     *
     * @param request the structured activity log data to be recorded.
     */
    @Override
    public void sendActivityLog(ActivityLogMessage request) { 
        log.info("Sending activity log message: {}", request);
        rabbitTemplate.convertAndSend("main-exchange", "activity-routing", request);
    }

    /**
     * Triggers an asynchronous email delivery for transaction invoices.
     * <p>Uses the {@code transaction-invoice-routing} key to signal the Mail Consumer.</p>
     *
     * @param request metadata required to generate and send the invoice email.
     */
    @Override
    public void sendTransactionInvoiceEmail(TransactionInvoiceMail request) {
        log.info("Sending transaction invoice email request: {}", request);
        rabbitTemplate.convertAndSend("main-exchange", "transaction-invoice-routing", request);
    }

    /**
     * Offloads file upload tasks to a dedicated background worker.
     * <p>
     * This method is used to keep the main application thread responsive by delegating 
     * cloud storage uploads (e.g., to Cloudinary) via the {@code upload-storage-routing} key.
     * </p>
     *
     * @param request details of the file and target folder for the storage operation.
     */
    @Override
    public void sendUploadToStorage(UploadStorageMessage request) {
        log.info("Sending upload-to-storage message: {}", request);
        rabbitTemplate.convertAndSend("main-exchange", "upload-storage-routing", request);
    }

    
    
}
