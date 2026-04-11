package lumi.insert.app.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any; 
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList; 
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder; 

import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.mail.MessagingException;
import lumi.insert.app.activitycore.entity.ActivityLog;
import lumi.insert.app.activitycore.entity.nondatabase.ActivityLogMessage;
import lumi.insert.app.activitycore.repository.ActivityLogRepository;
import lumi.insert.app.core.entity.TransactionPayment;
import lumi.insert.app.core.entity.nondatabase.CloudinaryResponse;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;
import lumi.insert.app.core.entity.nondatabase.EntityList;
import lumi.insert.app.core.entity.nondatabase.TransactionInvoiceMail;
import lumi.insert.app.core.entity.nondatabase.UploadStorageMessage;
import lumi.insert.app.core.repository.SupplyPaymentPictureRepository;
import lumi.insert.app.core.repository.SupplyPaymentRepository;
import lumi.insert.app.core.repository.TransactionPaymentPictureRepository;
import lumi.insert.app.core.repository.TransactionPaymentRepository;
import lumi.insert.app.exception.DatabaseInternalException;
import lumi.insert.app.service.StorageService; 
import lumi.insert.app.service.implement.MailSenderServiceImpl;

@ExtendWith(MockitoExtension.class)
public class MessageConsumerTest {
    
    @Mock
    private ActivityLogRepository repository;

    @InjectMocks
    private MessageConsumer consumer;

    @Mock
    MailSenderServiceImpl mailSenderService;

    @Mock
    StorageService storageService;

    @Mock
    TransactionPaymentRepository transactionPaymentRepository;

    @Mock
    TransactionPaymentPictureRepository transactionPaymentPictureRepository;

    @Mock
    SupplyPaymentRepository supplyPaymentRepository;

    @Mock
    SupplyPaymentPictureRepository supplyPaymentPictureRepository;

    @Test
    void activitLogsHandler_shouldSave(){
        ActivityLog activityLog = ActivityLog.builder()
        .actionMessage("a message")
        .build();

        consumer.activityLogsHandler(new ActivityLogMessage(activityLog));

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

    @Test
    void uploadStorageHandler_shouldUploadAndSave() throws Exception{
        SecurityContext securityContext = mock(SecurityContext.class);

        TransactionPayment transactionPayment = TransactionPayment.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .pictureUrl(new ArrayList<>(List.of("testFirst.test")))
        .build();

        CloudinaryResponse cloudinaryResponse = CloudinaryResponse.builder()
        .secureUrl("testUrl.test")
        .publicId("id123")
        .build();

        UploadStorageMessage uploadStorageMessage = new UploadStorageMessage(EntityList.TRANSACTION_PAYMENT, transactionPayment.getId(), "", EmployeeLogin.builder().build());

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(() -> SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(transactionPaymentRepository.findById(any())).thenReturn(Optional.of(transactionPayment));
            when(storageService.uploadImage(any(), any())).thenReturn(cloudinaryResponse);
            
            consumer.uploadStorageHandler(uploadStorageMessage);

            verify(transactionPaymentRepository, times(1)).findById(transactionPayment.getId());
            verify(transactionPaymentPictureRepository, times(1)).save(argThat(arg -> arg.getPictureUrl().equals(cloudinaryResponse.getSecureUrl())));

            List<String> pictureUrl = transactionPayment.getPictureUrl();
            assertTrue(pictureUrl.contains(cloudinaryResponse.getSecureUrl()));
            assertEquals(2, pictureUrl.size());
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    void uploadStorageHandler_failedToSave_shouldDeleteUploaded() throws Exception{
        SecurityContext securityContext = mock(SecurityContext.class);

        TransactionPayment transactionPayment = TransactionPayment.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .pictureUrl(new ArrayList<>(List.of("testFirst.test")))
        .build();

        CloudinaryResponse cloudinaryResponse = CloudinaryResponse.builder()
        .secureUrl("testUrl.test")
        .publicId("id123")
        .build();

        UploadStorageMessage uploadStorageMessage = new UploadStorageMessage(EntityList.TRANSACTION_PAYMENT, transactionPayment.getId(), "", EmployeeLogin.builder().build());

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(() -> SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(transactionPaymentRepository.findById(any())).thenReturn(Optional.of(transactionPayment));
            when(storageService.uploadImage(any(), any())).thenReturn(cloudinaryResponse);
            when(transactionPaymentPictureRepository.save(any())).thenThrow(DataIntegrityViolationException.class); 

            assertThrows(DatabaseInternalException.class, () -> consumer.uploadStorageHandler(uploadStorageMessage)); 

            verify(storageService, times(1)).deleteImage(cloudinaryResponse.getPublicId());
            List<String> pictureUrl = transactionPayment.getPictureUrl();
            assertFalse(pictureUrl.contains(cloudinaryResponse.getSecureUrl()));
            assertEquals(1, pictureUrl.size());
        } catch (Exception e) {
            throw e;
        }
    }
}
