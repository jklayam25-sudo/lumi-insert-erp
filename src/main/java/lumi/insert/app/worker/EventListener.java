package lumi.insert.app.worker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import lumi.insert.app.core.entity.nondatabase.UploadStorageMessage;
import lumi.insert.app.service.MessageProducerService;

@Component
public class EventListener {
    
    @Autowired
    MessageProducerService messageProducerService;

    @TransactionalEventListener()
    void afterCommit(UploadStorageMessage message){
        messageProducerService.sendUploadToStorage(message);
    }
}
