package lumi.insert.app.service.transactionitem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import lumi.insert.app.core.entity.TransactionItem;
import lumi.insert.app.core.repository.projection.ProductRefund;
import lumi.insert.app.core.repository.projection.ProductSale;
import lumi.insert.app.dto.request.PaginationRequest;
import lumi.insert.app.dto.response.TransactionItemResponse;
import lumi.insert.app.dto.response.TransactionItemStatisticResponse;
import lumi.insert.app.exception.NotFoundEntityException;

public class TransactionItemServiceGetTest extends BaseTransactionItemServiceTest{
    @Test
    @DisplayName("Should return slice of TransactionItemResponce when transaction item found")
    public void getTransactionItemsByTransactionId_validId_returnSliceOfTransactionItem(){
        Slice<TransactionItem> slice = new SliceImpl<>(List.of(setupTransactionItem));

        when(transactionItemRepositoryMock.findAllByTransactionId(any(UUID.class), any(Pageable.class))).thenReturn(slice);

        PaginationRequest request = PaginationRequest.builder() 
        .build();
        
        Slice<TransactionItemResponse> transactionItemsByTransactionId = transactionItemServiceMock.getTransactionItemsByTransactionId(UuidCreator.getTimeOrderedEpochFast(), request);  
        assertEquals(1, transactionItemsByTransactionId.getNumberOfElements());
        assertEquals(setupTransactionItem.getPrice(), transactionItemsByTransactionId.getContent().getFirst().price());                                                   
    } 
 

    @Test
    @DisplayName("Should return TransactionItemResponse when trxitem found")
    public void getTransactionItemByTransactionIdAndProductId_validId_throwNotFoundError(){ 
        when(transactionItemRepositoryMock.findByTransactionIdAndProductId(any(), any())).thenReturn(List.of(setupTransactionItem)); 

         Slice<TransactionItemResponse> transactionByTransactionIdAndProductId = transactionItemServiceMock.getTransactionByTransactionIdAndProductId(null, null);

         assertEquals(1, transactionByTransactionIdAndProductId.getNumberOfElements());
         assertEquals(setupTransactionItem.getId(), transactionByTransactionIdAndProductId.getContent().getFirst().id());
    }

    @Test
    @DisplayName("Should return TransactionItemResponse when trxitem found")
    public void getTransactionItem_validId_returnTrxItemResponse(){ 
        when(transactionItemRepositoryMock.findById(any())).thenReturn(Optional.of(setupTransactionItem)); 

        TransactionItemResponse TransactionItemResponse = transactionItemServiceMock.getTransactionItem(null);

         assertEquals(setupTransactionItem.getId(), TransactionItemResponse.id());
    }

    @Test
    @DisplayName("Should thrown not found error when transaction item not found")
    public void getTransactionItem_invalidId_throwNotFoundError(){ 
        when(transactionItemRepositoryMock.findById(any())).thenReturn(Optional.empty()); 

        assertThrows(NotFoundEntityException.class, () -> transactionItemServiceMock.getTransactionItem(UuidCreator.getTimeOrderedEpochFast()));
    }

    @Test
    @DisplayName("Should return TransactionItemStatisticResponse (List of productSale and List of productRefund) when found any")
    public void getTransactionItemStats_foundAny_returnTransactionItemStatisticResponse(){ 
        ProductSale productSale = new ProductSale("SomeProduct", BigDecimal.valueOf(10L));
        ProductRefund productRefund = new ProductRefund("SomeProduct", BigDecimal.valueOf(10L));
        when(transactionItemRepositoryMock.getProductTopSales(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(productSale)); 
        when(transactionItemRepositoryMock.getProductTopRefund(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(productRefund)); 

        TransactionItemStatisticResponse transactionItemStats = transactionItemServiceMock.getTransactionItemStats(LocalDateTime.now(), LocalDateTime.now());
        assertEquals(productSale, transactionItemStats.getProductSales().getFirst());
        assertEquals(productRefund, transactionItemStats.getProductRefunds().getFirst());
    }
}
