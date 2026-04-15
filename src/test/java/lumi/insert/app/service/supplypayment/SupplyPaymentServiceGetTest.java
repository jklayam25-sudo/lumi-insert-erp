package lumi.insert.app.service.supplypayment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;

import lumi.insert.app.core.entity.SupplyPayment;
import lumi.insert.app.dto.request.PaginationRequest;
import lumi.insert.app.dto.request.SupplyPaymentGetByFilter; 
import lumi.insert.app.dto.response.SupplyPaymentResponse;
import lumi.insert.app.exception.NotFoundEntityException;

public class SupplyPaymentServiceGetTest extends BaseSupplyPaymentServiceTest{

    @Test
    @DisplayName("Should return SupplyPaymentResponce when supply payment found")
    public void getSupplyPayment_validId_returnSupplyPaymentDTO(){
        setupSupplyPayment.setTotalPayment(BigDecimal.valueOf(1000L));
        when(supplyPaymentRepositoryMock.findById(setupSupplyPayment.getId())).thenReturn(Optional.of(setupSupplyPayment));

        SupplyPaymentResponse result = supplyPaymentServiceMock.getSupplyPayment(setupSupplyPayment.getId());
        assertEquals(setupSupplyPayment.getId(), result.id());
        assertTrue(BigDecimal.valueOf(1000L).compareTo(result.totalPayment()) == 0);
    }

    @Test
    @DisplayName("Should thrown not found error when supply payment not found")
    public void getSupplyPayment_invalidId_throwNotFoundError(){ 
        when(supplyPaymentRepositoryMock.findById(any())).thenReturn(Optional.empty()); 

        assertThrows(NotFoundEntityException.class, () -> supplyPaymentServiceMock.getSupplyPayment(null));
    }

    @Test
    @DisplayName("Should return Slice of SupplyPaymentResponce when supply payment with containing trx id found")
    public void getSupplyPaymentBySupplyId_validId_returnSliceSupplyPaymentDTO(){
        setupSupplyPayment.setTotalPayment(BigDecimal.valueOf(1000L));
        Slice<SupplyPayment> slices = new SliceImpl<>(List.of(setupSupplyPayment));
        when(supplyPaymentRepositoryMock.findAllBySupplyId(eq(setupSupplyPayment.getId()), any())).thenReturn(slices);

        Slice<SupplyPaymentResponse> result = supplyPaymentServiceMock.getSupplyPaymentsBySupplyId(setupSupplyPayment.getId(), PaginationRequest.builder().build());
        assertEquals(setupSupplyPayment.getId(), result.getContent().getFirst().id());
        assertTrue(BigDecimal.valueOf(1000L).compareTo(result.getContent().getFirst().totalPayment()) == 0);
        assertEquals(1, result.getNumberOfElements());
    }

    @Test
    @DisplayName("Should return Slice of SupplyPaymentResponce when supply payment with containing trx id found")
    public void getSupplyPaymentBySupplyId_notFound_returnEmptySliceSupplyPaymentDTO(){ 
        Slice<SupplyPayment> slices = new SliceImpl<>(List.of());
        when(supplyPaymentRepositoryMock.findAllBySupplyId(eq(setupSupplyPayment.getId()), any())).thenReturn(slices);

        Slice<SupplyPaymentResponse> result = supplyPaymentServiceMock.getSupplyPaymentsBySupplyId(setupSupplyPayment.getId(), PaginationRequest.builder().build());
        assertEquals(List.of(), result.getContent());
        assertTrue(result.isEmpty());
        assertEquals(0, result.getNumberOfElements());
    }

    @Test
    @DisplayName("Should return Slice of SupplyPaymentResponce when supply payment with containing trx id found")
    public void getSupplyPaymentsByRequest_validId_returnSliceSupplyPaymentDTO(){
        setupSupplyPayment.setTotalPayment(BigDecimal.valueOf(1000L));
        Page<SupplyPayment> slices = new PageImpl<SupplyPayment>((List.of(setupSupplyPayment)));
        when(jpaSpecGenerator.pageable(any())).thenReturn(PageRequest.of(0, 10));
        when(jpaSpecGenerator.supplyPaymentSpecification(any())).thenReturn(Specification.anyOf(List.of()));
        when(supplyPaymentRepositoryMock.findAll(ArgumentMatchers.<Specification<SupplyPayment>>any(), any(Pageable.class))).thenReturn(slices);

        Slice<SupplyPaymentResponse> result = supplyPaymentServiceMock.getSupplyPaymentsByRequests(SupplyPaymentGetByFilter.builder().build());
        assertEquals(setupSupplyPayment.getId(), result.getContent().getFirst().id());
        assertTrue(BigDecimal.valueOf(1000L).compareTo(result.getContent().getFirst().totalPayment()) == 0);
        assertEquals(1, result.getNumberOfElements());
    }

    @Test
    @DisplayName("Should return Slice of SupplyPaymentResponce when supply payment with containing trx id found")
    public void getSupplyPaymentsByRequest_notFoundAny_returnEmptySliceSupplyPaymentDTO(){ 
        Page<SupplyPayment> slices = new PageImpl<SupplyPayment>((List.of()));
        when(jpaSpecGenerator.pageable(any())).thenReturn(PageRequest.of(0, 10));
        when(jpaSpecGenerator.supplyPaymentSpecification(any())).thenReturn(Specification.anyOf(List.of()));
        when(supplyPaymentRepositoryMock.findAll(ArgumentMatchers.<Specification<SupplyPayment>>any(), any(Pageable.class))).thenReturn(slices);

        Slice<SupplyPaymentResponse> result = supplyPaymentServiceMock.getSupplyPaymentsByRequests(SupplyPaymentGetByFilter.builder().build());
        assertEquals(List.of(), result.getContent());
        assertTrue(result.isEmpty());
        assertEquals(0, result.getNumberOfElements());
    }
}
