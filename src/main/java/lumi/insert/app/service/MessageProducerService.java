package lumi.insert.app.service;
  
import lumi.insert.app.activitycore.entity.ActivityLog;
import lumi.insert.app.core.entity.nondatabase.TransactionInvoiceMail;
import lumi.insert.app.core.entity.nondatabase.UploadStorageMessage;

public interface MessageProducerService {

    void sendActivityLog(ActivityLog activityLog);

    void sendTransactionInvoiceEmail(TransactionInvoiceMail request);

    void sendUploadToStorage(UploadStorageMessage request);
}
