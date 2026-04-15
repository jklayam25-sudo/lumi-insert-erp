package lumi.insert.app.service.supplypayment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
    public void createSupplyPayment_validRequest_returnSupplyPaymentResponse() {
        setupSupplier.setTotalUnpaid(BigDecimal.valueOf(1500000L));
        setupSupplier.setTotalPaid(BigDecimal.valueOf(200000L));

        setupSupply.setSupplier(setupSupplier);
        setupSupply.setTotalUnpaid(BigDecimal.valueOf(1000000L));
        setupSupply.setTotalPaid(BigDecimal.valueOf(200000L));
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
            .paymentFrom("BCA - XXXXXX")
            .paymentTo("SG BANK - 12XXXXXX")
            .totalPayment(BigDecimal.valueOf(523000L))
            .files(new MultipartFile[]{})
            .build();

        when(supplyPaymentRepositoryMock.save(any())).thenAnswer((res) -> res.getArgument(0));

        SupplyPaymentResponse supplyPayment = supplyPaymentServiceMock.createSupplyPayment(setupSupply.getId(), request);

        assertFalse(supplyPayment.isForRefund());
        assertTrue(BigDecimal.valueOf(523000L).compareTo(supplyPayment.totalPayment()) == 0);
        assertEquals(request.getPaymentFrom(), supplyPayment.paymentFrom());
        assertEquals(setupSupply.getId(), supplyPayment.supplyId());
        
        // Asserting calculated balances
        assertTrue(BigDecimal.valueOf(477000L).compareTo(setupSupply.getTotalUnpaid()) == 0); // 1,000,000 - 523,000
        assertTrue(BigDecimal.valueOf(723000L).compareTo(setupSupply.getTotalPaid()) == 0);   // 200,000 + 523,000

        assertTrue(BigDecimal.valueOf(977000L).compareTo(setupSupplier.getTotalUnpaid()) == 0); // 1,500,000 - 523,000
        assertTrue(BigDecimal.valueOf(723000L).compareTo(setupSupplier.getTotalPaid()) == 0);   // 200,000 + 523,000
    }

    @Test
    @DisplayName("Should set supply complete and calcute Supply total , return SupplyPaymentResponse DTO when creating supply payment is successful")
    public void createSupplyPayment_fullPayment_returnSupplyPaymentResponse() {
        setupSupplier.setTotalUnpaid(BigDecimal.valueOf(1500000L));
        setupSupplier.setTotalPaid(BigDecimal.valueOf(200000L));

        setupSupply.setSupplier(setupSupplier);
        setupSupply.setTotalUnpaid(BigDecimal.valueOf(1000000L));
        setupSupply.setTotalPaid(BigDecimal.valueOf(200000L));
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
            .paymentFrom("BCA - XXXXXX")
            .paymentTo("SG BANK - 12XXXXXX")
            .totalPayment(BigDecimal.valueOf(1000000L))
            .files(new MultipartFile[]{})
            .build();

        when(supplyPaymentRepositoryMock.save(any())).thenAnswer((res) -> res.getArgument(0));

        supplyPaymentServiceMock.createSupplyPayment(setupSupply.getId(), request);

        assertEquals(SupplyStatus.COMPLETE, setupSupply.getStatus());  
    }

    @Test
    @DisplayName("Should thrown not found error when supply not found")
    public void createSupplyPayment_invalidId_throwNotFoundError() { 
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundEntityException.class, () -> supplyPaymentServiceMock.createSupplyPayment(null, new SupplyPaymentCreateRequest()));
    }

    @Test
    @DisplayName("Should throw forbidden req exc when supply payment status is not UNPAID")
    public void createSupplyPayment_notUnpaidStatus_throwForbiddenReq() { 
        setupSupply.setStatus(SupplyStatus.CANCELLED);
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
            .paymentFrom("BCA - XXXXXX")
            .paymentTo("SG BANK - 12XXXXXX")
            .totalPayment(BigDecimal.valueOf(1000000L))
            .files(new MultipartFile[]{})
            .build(); 

        assertThrows(ForbiddenRequestException.class, ()-> supplyPaymentServiceMock.createSupplyPayment(setupSupply.getId(), request));
    }

    @Test
    @DisplayName("Should thrown trxValidate error when supply total debt/unpaid lesser than request total payment < Overpayment")
    public void createSupplyPayment_overPayment_throwSupplyValidateError() {
        setupSupply.setTotalUnpaid(BigDecimal.valueOf(10000L));
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
            .paymentFrom("BCA - XXXXXX")
            .paymentTo("SG BANK - 12XXXXXX")
            .totalPayment(BigDecimal.valueOf(523000L))
            .files(new MultipartFile[]{})
            .build();

        assertThrows(TransactionValidationException.class, ()-> supplyPaymentServiceMock.createSupplyPayment(setupSupply.getId(), request));
    }

    @Test
    @DisplayName("Should publish upload event when files are present")
    public void createSupplyPayment_uploadImage_shouldPublishEvent() {
        setupSupplier.setTotalUnpaid(BigDecimal.valueOf(1500000L));
        setupSupplier.setTotalPaid(BigDecimal.valueOf(200000L));

        setupSupply.setSupplier(setupSupplier);
        setupSupply.setTotalUnpaid(BigDecimal.valueOf(1000000L));
        setupSupply.setTotalPaid(BigDecimal.valueOf(200000L));
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
            .paymentFrom("BCA - XXXXXX")
            .paymentTo("SG BANK - 12XXXXXX")
            .totalPayment(BigDecimal.valueOf(523000L))
            .files(new MultipartFile[]{new MockMultipartFile("Test", "ff".getBytes())})
            .build();

        when(supplyPaymentRepositoryMock.save(any())).thenAnswer((res) -> res.getArgument(0));

        supplyPaymentServiceMock.createSupplyPayment(setupSupply.getId(), request);
        
        ArgumentCaptor<UploadStorageMessage> capture = ArgumentCaptor.forClass(UploadStorageMessage.class);
        verify(applicationEventPublisher, times(1)).publishEvent(capture.capture());

        assertEquals(EntityList.SUPPLY_PAYMENT, capture.getValue().entity());
    }

    @Test
    @DisplayName("Should calcute Supply refund debt when creating refund supply payment is successful")
    public void refundSupplyPayment_nonFullPayment_returnSupplyPaymentResponse() {
        setupSupplier.setTotalUnpaid(BigDecimal.ZERO);
        setupSupplier.setTotalPaid(BigDecimal.valueOf(200000L));
        setupSupplier.setTotalUnrefunded(BigDecimal.valueOf(11000L));
        setupSupplier.setTotalRefunded(BigDecimal.valueOf(8000L));

        setupSupply.setSupplier(setupSupplier);
        setupSupply.setTotalUnpaid(BigDecimal.ZERO);
        setupSupply.setTotalPaid(BigDecimal.valueOf(200000L));
        setupSupply.setTotalUnrefunded(BigDecimal.valueOf(10000L));
        setupSupply.setTotalRefunded(BigDecimal.valueOf(7000L));
        setupSupply.setStatus(SupplyStatus.COMPLETE);
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
            .paymentFrom("BCA - XXXXXX")
            .paymentTo("SG BANK - 12XXXXXX")
            .totalPayment(BigDecimal.valueOf(10000L))
            .files(new MultipartFile[]{})
            .build();

        when(supplyPaymentRepositoryMock.save(any())).thenAnswer((res) -> res.getArgument(0));

        SupplyPaymentResponse supplyPayment = supplyPaymentServiceMock.refundSupplyPayment(setupSupply.getId(), request);

        assertTrue(BigDecimal.valueOf(10000L).compareTo(supplyPayment.totalPayment()) == 0);
        assertTrue(supplyPayment.isForRefund());
        
        assertTrue(BigDecimal.ZERO.compareTo(setupSupply.getTotalUnrefunded()) == 0); 
        assertTrue(BigDecimal.valueOf(17000L).compareTo(setupSupply.getTotalRefunded()) == 0); // 7000 + 10000

        assertTrue(BigDecimal.valueOf(1000L).compareTo(setupSupplier.getTotalUnrefunded()) == 0); // 11000 - 10000
        assertTrue(BigDecimal.valueOf(18000L).compareTo(setupSupplier.getTotalRefunded()) == 0);   // 8000 + 10000
    }

    @Test 
    @DisplayName("Should publish event when refund supply payment includes an image upload")
    public void refundSupplyPayment_uploadImage_shouldPublishEvent(){
        setupSupplier.setTotalUnpaid(BigDecimal.ZERO);
        setupSupplier.setTotalPaid(BigDecimal.valueOf(200000L));
        setupSupplier.setTotalUnrefunded(BigDecimal.valueOf(11000L));
        setupSupplier.setTotalRefunded(BigDecimal.valueOf(8000L));

        setupSupply.setSupplier(setupSupplier);
        setupSupply.setTotalUnpaid(BigDecimal.ZERO);
        setupSupply.setTotalPaid(BigDecimal.valueOf(200000L));
        setupSupply.setTotalUnrefunded(BigDecimal.valueOf(10000L));
        setupSupply.setTotalRefunded(BigDecimal.valueOf(7000L));
        setupSupply.setStatus(SupplyStatus.COMPLETE);
        
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
            .paymentFrom("BCA - XXXXXX")
            .paymentTo("SG BANK - 12XXXXXX")
            .totalPayment(BigDecimal.valueOf(10000L))
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
            .paymentTo("BCA - XXXXXdX")
            .paymentFrom("OUR COMPANY.SG BANK - 12XXXXXX")
            .totalPayment(BigDecimal.valueOf(1000000L))
            .files(new MultipartFile[]{})
            .build();

        assertThrows(NotFoundEntityException.class, () -> 
            supplyPaymentServiceMock.refundSupplyPayment(setupSupplyPayment.getId(), request));
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
            .totalPayment(BigDecimal.valueOf(1000000L))
            .files(new MultipartFile[]{})
            .build();

        assertThrows(ForbiddenRequestException.class, () -> 
            supplyPaymentServiceMock.refundSupplyPayment(setupSupplyPayment.getId(), request));
    }

    @Test
    @DisplayName("Should throw trxValidation when creating over payment refund")
    public void refundSupplyPayment_overPayment_returnSupplyPaymentResponse() {
        setupSupply.setTotalUnrefunded(BigDecimal.valueOf(1000000L));
        setupSupply.setTotalRefunded(BigDecimal.valueOf(12000L));
        setupSupply.setStatus(SupplyStatus.CANCELLED);

        setupSupplyPayment.setSupply(setupSupply);
        when(supplyRepositoryMock.findById(any())).thenReturn(Optional.of(setupSupply));

        SupplyPaymentCreateRequest request = SupplyPaymentCreateRequest.builder()
            .paymentTo("BCA - XXXXXX")
            .paymentFrom("OUR COMPANY.SG BANK - 12XXXXXX")
            .totalPayment(BigDecimal.valueOf(109900000L))
            .files(new MultipartFile[]{})
            .build();
 
        assertThrows(TransactionValidationException.class, () -> supplyPaymentServiceMock.refundSupplyPayment(setupSupplyPayment.getId(), request));
    }
}
