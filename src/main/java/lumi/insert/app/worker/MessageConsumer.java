package lumi.insert.app.worker; 

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.activitycore.entity.ActivityLog;
import lumi.insert.app.activitycore.repository.ActivityLogRepository;
import lumi.insert.app.core.entity.SupplyPayment;
import lumi.insert.app.core.entity.SupplyPaymentPicture;
import lumi.insert.app.core.entity.TransactionPayment;
import lumi.insert.app.core.entity.TransactionPaymentPicture;
import lumi.insert.app.core.entity.nondatabase.CloudinaryResponse;
import lumi.insert.app.core.entity.nondatabase.EntityList;
import lumi.insert.app.core.entity.nondatabase.TransactionInvoiceMail;
import lumi.insert.app.core.entity.nondatabase.UploadStorageMessage;
import lumi.insert.app.core.repository.SupplyPaymentPictureRepository;
import lumi.insert.app.core.repository.SupplyPaymentRepository;
import lumi.insert.app.core.repository.TransactionPaymentPictureRepository;
import lumi.insert.app.core.repository.TransactionPaymentRepository;
import lumi.insert.app.exception.DatabaseInternalException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.StorageActionException;
import lumi.insert.app.service.MailSenderService;
import lumi.insert.app.service.StorageService;

@Component
@Slf4j
public class MessageConsumer {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private MailSenderService mailSenderService; 

    @Autowired
    private StorageService storageService;

    @Autowired
    private TransactionPaymentRepository trxPaymentRepository;

    @Autowired
    private TransactionPaymentPictureRepository trxPaymentPicRepository;

    @Autowired
    private SupplyPaymentRepository supplyPaymentRepository;

    @Autowired
    private SupplyPaymentPictureRepository supplyPaymentPictureRepository;

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

    @RabbitListener(queues = "upload-storage")
    @Transactional
    void uploadStorageHandler(UploadStorageMessage request){  
        EntityList entity = request.entity();
        File file = new File(request.path());
        String publicId = null;

        try {
            log.info("Entity id, {}", request.id());
            log.info("Received message, trying to upload...");
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(request.auth(), null, null));
            if(entity == EntityList.TRANSACTION_PAYMENT){

                TransactionPayment transactionPayment = trxPaymentRepository.findById(request.id())
                    .orElseThrow(() -> new NotFoundEntityException("Ters"));
                log.info("Get payment to update, {}", transactionPayment.getId());

                CloudinaryResponse uploadImage = storageService.uploadImage(Path.of(request.path()), "transactionPayment"); 
                publicId = uploadImage.getPublicId();
                log.info("Upload succesfully, {}", uploadImage.getSecureUrl());

                TransactionPaymentPicture transactionPaymentPicture = TransactionPaymentPicture.builder()
                    .id(UuidCreator.getTimeOrderedEpochFast())
                    .pictureUrl(uploadImage.getSecureUrl())
                    .publicId(uploadImage.getPublicId())
                    .transactionPayment(transactionPayment)
                    .build();

                trxPaymentPicRepository.save(transactionPaymentPicture);

                List<String> pictureUrl = transactionPayment.getPictureUrl();
                if(pictureUrl == null) pictureUrl = new ArrayList<>();

                pictureUrl.add(uploadImage.getSecureUrl());
                transactionPayment.setPictureUrl(pictureUrl);
            } else if (entity == EntityList.SUPPLY_PAYMENT){

                SupplyPayment supplyPayment = supplyPaymentRepository.findById(request.id())
                    .orElseThrow(() -> new NotFoundEntityException("Ters"));

                CloudinaryResponse uploadImage = storageService.uploadImage(Path.of(request.path()), "supplyPayment"); 
                publicId = uploadImage.getPublicId();

                SupplyPaymentPicture supplyPaymentPicture = SupplyPaymentPicture.builder()
                    .id(UuidCreator.getTimeOrderedEpochFast())
                    .pictureUrl(uploadImage.getSecureUrl())
                    .publicId(uploadImage.getPublicId())
                    .supplyPayment(supplyPayment)
                    .build();
                
                supplyPaymentPictureRepository.save(supplyPaymentPicture);

                List<String> pictureUrl = supplyPayment.getPictureUrl();
                if(pictureUrl == null) pictureUrl = new ArrayList<>();
                
                pictureUrl.add(uploadImage.getSecureUrl());
                supplyPayment.setPictureUrl(pictureUrl);
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Upload failed, messages: " + e.getMessage());
            throw new StorageActionException("Server couldn't complete the request due to internal problem, try again or contact developer");
        } catch (Exception e) { 
            e.printStackTrace();
            log.error("Save to database failed, attempting to delete image at storage. Messages: {}", e.getMessage()); 

            if(publicId != null) {
                if(!(storageService.deleteImage(publicId))) log.error("Failed to delete image with publicId: {}", publicId);
            }

            throw new DatabaseInternalException("Server couldn't complete the request due to internal problem, try again or contact developer");
        }  finally{
            file.deleteOnExit();
        }
            
    } 

}
