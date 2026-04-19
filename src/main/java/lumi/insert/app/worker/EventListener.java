package lumi.insert.app.worker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.core.entity.nondatabase.UploadStorageMessage;
import lumi.insert.app.service.MessageProducerService;

/**
 * EventPublish Listener.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Component
@Slf4j
public class EventListener {
    
    @Autowired
    MessageProducerService messageProducerService;

    /**
     * <p>Call {@link MessageProducerService} after commit</P>
     * Triggered after transactional commited 
     * @param message
     */
    @TransactionalEventListener()
    void afterCommit(UploadStorageMessage message){
        log.info("Processing upload storage event for entity: {}, id: {}", message.entity(), message.id());
        messageProducerService.sendUploadToStorage(message);
        log.debug("Upload storage message sent successfully: {}", message);
    }
}
