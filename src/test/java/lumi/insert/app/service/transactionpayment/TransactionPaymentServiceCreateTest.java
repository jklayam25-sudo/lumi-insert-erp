package lumi.insert.app.service.transactionpayment;

import static org.junit.jupiter.api.Assertions.assertEquals; 
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional; 

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import lumi.insert.app.core.entity.nondatabase.EntityList;
import lumi.insert.app.core.entity.nondatabase.TransactionStatus;
import lumi.insert.app.core.entity.nondatabase.UploadStorageMessage;
import lumi.insert.app.dto.request.TransactionPaymentCreateRequest; 
import lumi.insert.app.dto.response.TransactionPaymentResponse;
import lumi.insert.app.exception.ForbiddenRequestException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.TransactionValidationException;

public class TransactionPaymentServiceCreateTest extends BaseTransactionPaymentServiceTest{
    
    @Test
    @DisplayName("Should calcute Transaction total , return TransactionPaymentResponse DTO when creating transaction payment is successful")
    public void createTransactionPayment_validRequest_returnTransactionPaymentResponse(){
        setupTransaction.setCustomer(setupCustomer);
        setupTransaction.setTotalUnpaid(1000000L);
        when(transactionRepositoryMock.findById(any())).thenReturn(Optional.of(setupTransaction));

        TransactionPaymentCreateRequest request = TransactionPaymentCreateRequest.builder()
        .paymentFrom("BCA - XXXXXX")
        .paymentTo("SG BANK - 12XXXXXX")
        .totalPayment(523000L)
        .files(new MultipartFile[]{})
        .build();

        when(transactionPaymentRepositoryMock.save(any())).thenAnswer((res) -> res.getArgument(0));

        TransactionPaymentResponse transactionPayment = transactionPaymentServiceMock.createTransactionPayment(setupTransaction.getId(), request);

        assertEquals(523000L, transactionPayment.totalPayment());
        assertEquals(request.getPaymentFrom(), transactionPayment.paymentFrom());
        assertEquals(setupTransaction.getId(), transactionPayment.transactionId());
        assertEquals(477000L, setupTransaction.getTotalUnpaid());
        assertEquals(523000L, setupTransaction.getTotalPaid()); 
        
    }

    @Test
    @DisplayName("Should set transaction complete and calcute Transaction total , return TransactionPaymentResponse DTO when creating transaction payment is successful")
    public void createTransactionPayment_fullPayment_returnTransactionPaymentResponse(){
        setupTransaction.setCustomer(setupCustomer);
        setupTransaction.setTotalUnpaid(1000000L);
        when(transactionRepositoryMock.findById(any())).thenReturn(Optional.of(setupTransaction));

        TransactionPaymentCreateRequest request = TransactionPaymentCreateRequest.builder()
        .paymentFrom("BCA - XXXXXX")
        .paymentTo("SG BANK - 12XXXXXX")
        .totalPayment(1000000L)
        .files(new MultipartFile[]{})
        .build();

        when(transactionPaymentRepositoryMock.save(any())).thenAnswer((res) -> res.getArgument(0));

        transactionPaymentServiceMock.createTransactionPayment(setupTransaction.getId(), request);
  
        assertEquals(0L, setupTransaction.getTotalUnpaid());
        assertEquals(1000000L, setupTransaction.getTotalPaid()); 
        assertEquals(TransactionStatus.COMPLETE, setupTransaction.getStatus());
    }

    @Test
    @DisplayName("Should thrown not found error when transaction not found")
    public void createTransactionPayment_invalidId_throwNotFoundError(){ 
        when(transactionRepositoryMock.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundEntityException.class, () -> transactionPaymentServiceMock.createTransactionPayment(null, null));
    }

    @Test
    @DisplayName("Should thrown transactionValidate error when transaction total debt/unpaid lesser than request total payment < Overpayment")
    public void createTransactionPayment_overPayment_throwTransactionValidateError(){
        setupTransaction.setTotalUnpaid(10000L);
        when(transactionRepositoryMock.findById(any())).thenReturn(Optional.of(setupTransaction));

        TransactionPaymentCreateRequest request = TransactionPaymentCreateRequest.builder()
        .paymentFrom("BCA - XXXXXX")
        .paymentTo("SG BANK - 12XXXXXX")
        .totalPayment(523000L)
        .files(new MultipartFile[]{})
        .build();

        assertThrows(TransactionValidationException.class, ()-> transactionPaymentServiceMock.createTransactionPayment(setupTransaction.getId(), request));
    }

    @Test 
    public void createTransactionPayment_uploadImage_shouldPublishEvent(){
        setupTransaction.setCustomer(setupCustomer);
        setupTransaction.setTotalUnpaid(1000000L);
        when(transactionRepositoryMock.findById(any())).thenReturn(Optional.of(setupTransaction));

        TransactionPaymentCreateRequest request = TransactionPaymentCreateRequest.builder()
        .paymentFrom("BCA - XXXXXX")
        .paymentTo("SG BANK - 12XXXXXX")
        .totalPayment(523000L)
        .files(new MultipartFile[]{new MockMultipartFile("test", "ff".getBytes())})
        .build();

        when(transactionPaymentRepositoryMock.save(any())).thenAnswer((res) -> res.getArgument(0));

        transactionPaymentServiceMock.createTransactionPayment(setupTransaction.getId(), request);

        ArgumentCaptor<UploadStorageMessage> capture = ArgumentCaptor.forClass(UploadStorageMessage.class);
        verify(eventPublisher, times(1)).publishEvent(capture.capture());

        UploadStorageMessage value = capture.getValue();
        assertEquals(EntityList.TRANSACTION_PAYMENT, value.entity());
        
    }

    @Test
    @DisplayName("Should calcute Transaction refund debt , return TransactionPaymentResponse DTO when creating refund transaction payment is successful")
    public void refundTransactionPayment_nonFullPayment_returnTransactionPaymentResponse(){
        setupTransaction.setCustomer(setupCustomer);

        setupTransaction.setTotalUnrefunded(1000000L);
        setupTransaction.setTotalRefunded(12000L);
        setupTransaction.setStatus(TransactionStatus.PROCESS);

        setupTransactionPayment.setTransaction(setupTransaction);
        when(transactionRepositoryMock.findById(any())).thenReturn(Optional.of(setupTransaction));

        TransactionPaymentCreateRequest request = TransactionPaymentCreateRequest.builder()
        .paymentTo("BCA - XXXXXX")
        .paymentFrom("OUR COMPANY.SG BANK - 12XXXXXX")
        .totalPayment(900000L)
        .files(new MultipartFile[]{})
        .build();

        when(transactionPaymentRepositoryMock.save(any())).thenAnswer((res) -> res.getArgument(0));

        TransactionPaymentResponse refundTransactionPayment = transactionPaymentServiceMock.refundTransactionPayment(setupTransactionPayment.getId(), request);;
  
        assertEquals(100000L, setupTransaction.getTotalUnrefunded());
        assertEquals(912000L, setupTransaction.getTotalRefunded()); 
        assertEquals(TransactionStatus.PROCESS, setupTransaction.getStatus());
        assertTrue(refundTransactionPayment.isForRefund());
    }

    @Test 
    public void refundTransactionPayment_uploadImage_shouldPublishEvent(){
        setupTransaction.setCustomer(setupCustomer);

        setupTransaction.setTotalUnrefunded(1000000L);
        setupTransaction.setTotalRefunded(12000L);
        setupTransaction.setStatus(TransactionStatus.PROCESS);

        setupTransactionPayment.setTransaction(setupTransaction);
        when(transactionRepositoryMock.findById(any())).thenReturn(Optional.of(setupTransaction));

        TransactionPaymentCreateRequest request = TransactionPaymentCreateRequest.builder()
        .paymentTo("BCA - XXXXXX")
        .paymentFrom("OUR COMPANY.SG BANK - 12XXXXXX")
        .totalPayment(900000L)
        .files(new MultipartFile[]{new MockMultipartFile("Test", "ff".getBytes())})
        .build();

        when(transactionPaymentRepositoryMock.save(any())).thenAnswer((res) -> res.getArgument(0));

        transactionPaymentServiceMock.refundTransactionPayment(setupTransactionPayment.getId(), request);;
  
        ArgumentCaptor<UploadStorageMessage> capture = ArgumentCaptor.forClass(UploadStorageMessage.class);
        verify(eventPublisher, times(1)).publishEvent(capture.capture());

        UploadStorageMessage value = capture.getValue();
        assertEquals(EntityList.TRANSACTION_PAYMENT, value.entity());
    }

    @Test
    @DisplayName("Should set transaction complete and calcute Transaction refund debt , return TransactionPaymentResponse DTO when creating refund transaction payment is successful")
    public void refundTransactionPayment_fullPayment_returnTransactionPaymentResponse(){
        setupTransaction.setCustomer(setupCustomer);
        setupTransaction.setTotalUnrefunded(1000000L);
        setupTransaction.setTotalRefunded(12000L);
        setupTransaction.setStatus(TransactionStatus.CANCELLED);

        setupTransactionPayment.setTransaction(setupTransaction);
        when(transactionRepositoryMock.findById(any())).thenReturn(Optional.of(setupTransaction));

        TransactionPaymentCreateRequest request = TransactionPaymentCreateRequest.builder()
        .paymentTo("BCA - XXXXXX")
        .paymentFrom("OUR COMPANY.SG BANK - 12XXXXXX")
        .totalPayment(1000000L)
        .files(new MultipartFile[]{})
        .build();

        when(transactionPaymentRepositoryMock.save(any())).thenAnswer((res) -> res.getArgument(0));

        TransactionPaymentResponse refundTransactionPayment = transactionPaymentServiceMock.refundTransactionPayment(setupTransactionPayment.getId(), request);;
  
        assertEquals(0L, setupTransaction.getTotalUnrefunded());
        assertEquals(1012000L, setupTransaction.getTotalRefunded()); 
        assertEquals(TransactionStatus.COMPLETE, setupTransaction.getStatus());
        assertTrue(refundTransactionPayment.isForRefund());
    }

    @Test
    @DisplayName("Should throw NotFound when creating refund transaction to transaction that isn't found")
    public void refundTransactionPayment_notFoundTransaction_throwForbidden(){  
        when(transactionRepositoryMock.findById(any())).thenReturn(Optional.empty());

        TransactionPaymentCreateRequest request = TransactionPaymentCreateRequest.builder()
        .paymentTo("BCA - XXXXXX")
        .paymentFrom("OUR COMPANY.SG BANK - 12XXXXXX")
        .totalPayment(1000000L)
        .files(new MultipartFile[]{})
        .build();

        assertThrows(NotFoundEntityException.class, () -> transactionPaymentServiceMock.refundTransactionPayment(setupTransactionPayment.getId(), request));
    }

    @Test
    @DisplayName("Should throw ForbiddenRequest when creating refund transaction to transaction that isn't CANCELLED OR PROCESS")
    public void refundTransactionPayment_pendingTransaction_throwForbidden(){ 
        setupTransaction.setStatus(TransactionStatus.COMPLETE);
        setupTransactionPayment.setTransaction(setupTransaction);
        when(transactionRepositoryMock.findById(any())).thenReturn(Optional.of(setupTransaction));

        TransactionPaymentCreateRequest request = TransactionPaymentCreateRequest.builder()
        .paymentTo("BCA - XXXXXX")
        .paymentFrom("OUR COMPANY.SG BANK - 12XXXXXX")
        .totalPayment(1000000L)
        .files(new MultipartFile[]{})
        .build();

        assertThrows(ForbiddenRequestException.class, () -> transactionPaymentServiceMock.refundTransactionPayment(setupTransactionPayment.getId(), request));
    }

    @Test
    @DisplayName("Should throw TransactionValidation when creating over payment refund")
    public void refundTransactionPayment_overPayment_returnTransactionPaymentResponse(){
        setupTransaction.setTotalUnrefunded(1000000L);
        setupTransaction.setTotalRefunded(12000L);
        setupTransaction.setStatus(TransactionStatus.CANCELLED);

        setupTransactionPayment.setTransaction(setupTransaction);
        when(transactionRepositoryMock.findById(any())).thenReturn(Optional.of(setupTransaction));

        TransactionPaymentCreateRequest request = TransactionPaymentCreateRequest.builder()
        .paymentTo("BCA - XXXXXX")
        .paymentFrom("OUR COMPANY.SG BANK - 12XXXXXX")
        .totalPayment(109900000L)
        .files(new MultipartFile[]{})
        .build();
 
        assertThrows(TransactionValidationException.class, () -> transactionPaymentServiceMock.refundTransactionPayment(setupTransactionPayment.getId(), request));
    }
}
