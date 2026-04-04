package lumi.insert.app.service.supplypayment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import lumi.insert.app.core.entity.nondatabase.SupplyStatus;
import lumi.insert.app.core.entity.nondatabase.UploadStorageMessage;
import lumi.insert.app.dto.request.SupplyPaymentCreateRequest; 
import lumi.insert.app.dto.response.SupplyPaymentResponse;
import lumi.insert.app.exception.ForbiddenRequestException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.TransactionValidationException; 

public class SupplyPaymentServiceCreateTest extends BaseSupplyPaymentServiceTest{
    
    @Test
    @DisplayName("Should calcute Supply total , return SupplyPaymentResponse DTO when creating supply payment is successful")
    public void createSupplyPayment_validRequest_returnSupplyPaymentResponse(){
        setupSupplier.setTotalUnpaid(1500000L);
        setupSupplier.setTotalPaid(200000L);

        setupSupply.setSupplier(setupSupplier);
        setupSupply.setTotalUnpaid(1000000L);
        setupSupply.setTotalPaid(200000L);
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
        .paymentFrom("BCA - XXXXXX")
        .paymentTo("SG BANK - 12XXXXXX")
        .totalPayment(523000L)
        .files(new MultipartFile[]{})
        .build();

        when(supplyPaymentRepositoryMock.save(any())).thenAnswer((res) -> res.getArgument(0));

        SupplyPaymentResponse supplyPayment = supplyPaymentServiceMock.createSupplyPayment(setupSupply.getId(), request);

        assertFalse(supplyPayment.isForRefund());
        assertEquals(523000L, supplyPayment.totalPayment());
        assertEquals(request.getPaymentFrom(), supplyPayment.paymentFrom());
        assertEquals(setupSupply.getId(), supplyPayment.supplyId());
        assertEquals(477000L, setupSupply.getTotalUnpaid());
        assertEquals(523000L + 200000L, setupSupply.getTotalPaid()); 

        assertEquals(1500000L - request.getTotalPayment(), setupSupplier.getTotalUnpaid());
        assertEquals(request.getTotalPayment() + 200000L, setupSupplier.getTotalPaid()); 
        
    }

    @Test
    @DisplayName("Should set supply complete and calcute Supply total , return SupplyPaymentResponse DTO when creating supply payment is successful")
    public void createSupplyPayment_fullPayment_returnSupplyPaymentResponse(){
        setupSupplier.setTotalUnpaid(1500000L);
        setupSupplier.setTotalPaid(200000L);

        setupSupply.setSupplier(setupSupplier);
        setupSupply.setTotalUnpaid(1000000L);
        setupSupply.setTotalPaid(200000L);
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
        .paymentFrom("BCA - XXXXXX")
        .paymentTo("SG BANK - 12XXXXXX")
        .totalPayment(1000000L)
        .files(new MultipartFile[]{})
        .build();

        when(supplyPaymentRepositoryMock.save(any())).thenAnswer((res) -> res.getArgument(0));

        supplyPaymentServiceMock.createSupplyPayment(setupSupply.getId(), request);

        assertEquals(SupplyStatus.COMPLETE, setupSupply.getStatus());  
    }

    @Test
    @DisplayName("Should thrown not found error when supply not found")
    public void createSupplyPayment_invalidId_throwNotFoundError(){ 
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundEntityException.class, () -> supplyPaymentServiceMock.createSupplyPayment(null, null));
    }

    @Test
    @DisplayName("Should throw forbidden req exc when supply payment status is not UNPAID")
    public void createSupplyPayment_notUnpaidStatus_throwForbiddenReq(){ 
        setupSupply.setStatus(SupplyStatus.CANCELLED);
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
        .paymentFrom("BCA - XXXXXX")
        .paymentTo("SG BANK - 12XXXXXX")
        .totalPayment(1000000L)
        .files(new MultipartFile[]{})
        .build(); 

        assertThrows(ForbiddenRequestException.class, ()-> supplyPaymentServiceMock.createSupplyPayment(setupSupply.getId(), request));
    }

    @Test
    @DisplayName("Should thrown trxValidate error when supply total debt/unpaid lesser than request total payment < Overpayment")
    public void createSupplyPayment_overPayment_throwSupplyValidateError(){
        setupSupply.setTotalUnpaid(10000L);
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
        .paymentFrom("BCA - XXXXXX")
        .paymentTo("SG BANK - 12XXXXXX")
        .totalPayment(523000L)
        .files(new MultipartFile[]{})
        .build();

        assertThrows(TransactionValidationException.class, ()-> supplyPaymentServiceMock.createSupplyPayment(setupSupply.getId(), request));
    }

    @Test
    @DisplayName("Should calcute Supply total , return SupplyPaymentResponse DTO when creating supply payment is successful")
    public void createSupplyPayment_uploadImage_shouldPublishEvent(){
        setupSupplier.setTotalUnpaid(1500000L);
        setupSupplier.setTotalPaid(200000L);

        setupSupply.setSupplier(setupSupplier);
        setupSupply.setTotalUnpaid(1000000L);
        setupSupply.setTotalPaid(200000L);
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
        .paymentFrom("BCA - XXXXXX")
        .paymentTo("SG BANK - 12XXXXXX")
        .totalPayment(523000L)
        .files(new MultipartFile[]{new MockMultipartFile("Test", "ff".getBytes())})
        .build();

        when(supplyPaymentRepositoryMock.save(any())).thenAnswer((res) -> res.getArgument(0));

        supplyPaymentServiceMock.createSupplyPayment(setupSupply.getId(), request);
        ArgumentCaptor<UploadStorageMessage> capture = ArgumentCaptor.forClass(UploadStorageMessage.class);
        verify(applicationEventPublisher, times(1)).publishEvent(capture.capture());

        UploadStorageMessage value = capture.getValue();
        assertEquals(EntityList.SUPPLY_PAYMENT, value.entity());
        
    }

    @Test
    @DisplayName("Should calcute Supply refund debt , return SupplyPaymentResponse DTO when creating refund supply payment is successful")
    public void refundSupplyPayment_nonFullPayment_returnSupplyPaymentResponse(){
        setupSupplier.setTotalUnpaid(0L);
        setupSupplier.setTotalPaid(200000L);
        setupSupplier.setTotalUnrefunded(11000L);
        setupSupplier.setTotalRefunded(8000L);

        setupSupply.setSupplier(setupSupplier);
        setupSupply.setTotalUnpaid(0L);
        setupSupply.setTotalPaid(200000L);
        setupSupply.setTotalUnrefunded(10000L);
        setupSupply.setTotalRefunded(7000L);
        setupSupply.setStatus(SupplyStatus.COMPLETE);
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
        .paymentFrom("BCA - XXXXXX")
        .paymentTo("SG BANK - 12XXXXXX")
        .totalPayment(10000L)
        .files(new MultipartFile[]{})
        .build();

        when(supplyPaymentRepositoryMock.save(any())).thenAnswer((res) -> res.getArgument(0));

        SupplyPaymentResponse supplyPayment = supplyPaymentServiceMock.refundSupplyPayment(setupSupply.getId(), request);

        assertEquals(10000L, supplyPayment.totalPayment());
        assertTrue(supplyPayment.isForRefund());
        assertEquals(request.getPaymentFrom(), supplyPayment.paymentFrom());
        assertEquals(setupSupply.getId(), supplyPayment.supplyId());
        
        assertEquals(0L, setupSupply.getTotalUnpaid());
        assertEquals(0L, setupSupply.getTotalUnrefunded()); 
        assertEquals(7000L + 10000L, setupSupply.getTotalRefunded()); 

        assertEquals(11000L - request.getTotalPayment(), setupSupplier.getTotalUnrefunded());
        assertEquals(request.getTotalPayment() + 8000L, setupSupplier.getTotalRefunded());   
    }

    @Test 
    public void refundSupplyPayment_uploadImage_shouldPublishEvent(){
        setupSupplier.setTotalUnpaid(0L);
        setupSupplier.setTotalPaid(200000L);
        setupSupplier.setTotalUnrefunded(11000L);
        setupSupplier.setTotalRefunded(8000L);

        setupSupply.setSupplier(setupSupplier);
        setupSupply.setTotalUnpaid(0L);
        setupSupply.setTotalPaid(200000L);
        setupSupply.setTotalUnrefunded(10000L);
        setupSupply.setTotalRefunded(7000L);
        setupSupply.setStatus(SupplyStatus.COMPLETE);
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
        .paymentFrom("BCA - XXXXXX")
        .paymentTo("SG BANK - 12XXXXXX")
        .totalPayment(10000L)
        .files(new MultipartFile[]{new MockMultipartFile("Test", "ff".getBytes())})
        .build();

        when(supplyPaymentRepositoryMock.save(any())).thenAnswer((res) -> res.getArgument(0));

        supplyPaymentServiceMock.refundSupplyPayment(setupSupply.getId(), request);

        ArgumentCaptor<UploadStorageMessage> capture = ArgumentCaptor.forClass(UploadStorageMessage.class);
        verify(applicationEventPublisher, times(1)).publishEvent(capture.capture());

        UploadStorageMessage value = capture.getValue();
        assertEquals(EntityList.SUPPLY_PAYMENT, value.entity());   
    }
 

    @Test
    @DisplayName("Should throw NotFound when creating refund supply to supply that isn't found")
    public void refundSupplyPayment_notFoundSupply_throwNOtFound(){  
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.empty());

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
        .paymentTo("BCA - XXXXXX")
        .paymentFrom("OUR COMPANY.SG BANK - 12XXXXXX")
        .totalPayment(1000000L)
        .files(new MultipartFile[]{})
        .build();

        assertThrows(NotFoundEntityException.class, () -> supplyPaymentServiceMock.refundSupplyPayment(setupSupplyPayment.getId(), request));
    }

    @Test
    @DisplayName("Should throw ForbiddenRequest when creating refund supply to supply that isn't CANCELLED OR PROCESS")
    public void refundSupplyPayment_unPaidSupply_throwForbidden(){ 
        setupSupply.setStatus(SupplyStatus.UNPAID);
        setupSupplyPayment.setSupply(setupSupply);
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
        .paymentTo("BCA - XXXXXX")
        .paymentFrom("OUR COMPANY.SG BANK - 12XXXXXX")
        .totalPayment(1000000L)
        .files(new MultipartFile[]{})
        .build();

        assertThrows(ForbiddenRequestException.class, () -> supplyPaymentServiceMock.refundSupplyPayment(setupSupplyPayment.getId(), request));
    }

    @Test
    @DisplayName("Should throw trxValidation when creating over payment refund")
    public void refundSupplyPayment_overPayment_returnSupplyPaymentResponse(){
        setupSupply.setTotalUnrefunded(1000000L);
        setupSupply.setTotalRefunded(12000L);
        setupSupply.setStatus(SupplyStatus.CANCELLED);

        setupSupplyPayment.setSupply(setupSupply);
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
        .paymentTo("BCA - XXXXXX")
        .paymentFrom("OUR COMPANY.SG BANK - 12XXXXXX")
        .totalPayment(109900000L)
        .files(new MultipartFile[]{})
        .build();
 
        assertThrows(TransactionValidationException.class, () -> supplyPaymentServiceMock.refundSupplyPayment(setupSupplyPayment.getId(), request));
    }
}
