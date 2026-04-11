package lumi.insert.app.service;
   
import lumi.insert.app.activitycore.entity.nondatabase.ActivityLogMessage;
import lumi.insert.app.core.entity.nondatabase.TransactionInvoiceMail;
import lumi.insert.app.core.entity.nondatabase.UploadStorageMessage;

public interface MessageProducerService {

    void sendActivityLog(ActivityLogMessage request);

    void sendTransactionInvoiceEmail(TransactionInvoiceMail request);

    void sendUploadToStorage(UploadStorageMessage request);
}
