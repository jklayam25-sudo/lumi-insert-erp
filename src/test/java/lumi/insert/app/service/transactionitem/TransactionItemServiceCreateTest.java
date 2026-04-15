package lumi.insert.app.service.transactionitem;

import static org.junit.jupiter.api.Assertions.assertEquals; 
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.f4b6a3.uuid.UuidCreator;

import lumi.insert.app.core.entity.Transaction;
import lumi.insert.app.core.entity.TransactionItem;
import lumi.insert.app.core.entity.nondatabase.StockMove;
import lumi.insert.app.core.entity.nondatabase.TransactionStatus;
import lumi.insert.app.dto.request.ItemRefundRequest;
import lumi.insert.app.dto.request.TransactionItemCreateRequest;
import lumi.insert.app.dto.response.TransactionItemResponse;
import lumi.insert.app.exception.ForbiddenRequestException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.TransactionValidationException; 

public class TransactionItemServiceCreateTest extends BaseTransactionItemServiceTest {
    
    @Test
    @DisplayName("Should calcute Transaction total , return TransactionItemResponse DTO when creating transaction item is successful")
    public void createTransactionItem_validRequest_returnTransactionItemResponse(){
        when(productRepositoryMock.findById(999L)).thenReturn(Optional.of(setupProduct));
        when(transactionRepositoryMock.findById(any())).thenReturn(Optional.of(setupTransaction));

        TransactionItemCreateRequest request = TransactionItemCreateRequest.builder()
            .productId(999L)
            .quantity(BigDecimal.valueOf(9L))
            .build();

        TransactionItemResponse transactionItem = transactionItemServiceMock.createTransactionItem(null, request);

        assertTrue(BigDecimal.valueOf(19000L).compareTo(transactionItem.price()) == 0);
        assertTrue(BigDecimal.valueOf(9L).compareTo(transactionItem.quantity()) == 0);
        assertEquals(999L, transactionItem.productId());
        assertTrue(BigDecimal.valueOf(10L).compareTo(setupProduct.getStockQuantity()) == 0);
        assertTrue(BigDecimal.valueOf(171000L).compareTo(setupTransaction.getGrandTotal()) == 0); // 19000 * 9 
        assertEquals(1L, setupTransaction.getTotalItems()); 
    }

    @Test
    @DisplayName("Should thrown not found error when transaction not found")
    public void createTransactionItem_invalidId_throwNotFoundError(){ 
        when(transactionRepositoryMock.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundEntityException.class, () -> transactionItemServiceMock.createTransactionItem(null, TransactionItemCreateRequest.builder().build()));
    }

    @Test
    @DisplayName("Should thrown not found error when product not found")
    public void createTransactionItem_invalidProductId_throwNotFoundError(){ 
        when(transactionRepositoryMock.findById(any())).thenReturn(Optional.of(Transaction.builder().build()));
        when(productRepositoryMock.findById(any())).thenReturn(Optional.empty());

        TransactionItemCreateRequest request = TransactionItemCreateRequest.builder().productId(1L).build();
        assertThrows(NotFoundEntityException.class, () -> transactionItemServiceMock.createTransactionItem(null, request));
    }

    @Test
    @DisplayName("Should thrown transactionValidate error when product stock lesser than request buy stock")
    public void createTransactionItem_outOfStock_throwTransactionValidateError(){
        when(productRepositoryMock.findById(999L)).thenReturn(Optional.of(setupProduct));
        when(transactionRepositoryMock.findById(any())).thenReturn(Optional.of(setupTransaction));

        TransactionItemCreateRequest request = TransactionItemCreateRequest.builder()
            .productId(999L)
            .quantity(BigDecimal.valueOf(19L))
            .build();

        assertThrows(TransactionValidationException.class, () -> transactionItemServiceMock.createTransactionItem(null, request));
    }

    @Test
    @DisplayName("Should calcute Transaction total , return TransactionItemResponse DTO when refund action transaction item is successful")
    public void refundTransactionItem_validRequest_returnTransactionItemResponse(){
        
        setupProduct.setSellPrice(BigDecimal.valueOf(1050L));
        setupProduct.setStockQuantity(BigDecimal.valueOf(1L));

        setupTransaction.setStatus(TransactionStatus.COMPLETE);
        setupTransaction.setTotalUnpaid(BigDecimal.valueOf(3000L));
        setupCustomer.setTotalUnpaid(BigDecimal.valueOf(2000L));
        setupTransaction.setCustomer(setupCustomer);

        setupTransactionItem.setPrice(BigDecimal.valueOf(1000L));
        setupTransactionItem.setQuantity(BigDecimal.valueOf(3L));
        setupTransactionItem.setTransaction(setupTransaction);
        setupTransactionItem.setProduct(setupProduct); 

        when(transactionItemRepositoryMock.findByTransactionIdAndProductId(any(), anyLong())).thenReturn(List.of(setupTransactionItem));
        when(transactionItemRepositoryMock.save(any())).thenAnswer(res -> res.getArgument(0));
        when(stockCardRepositoryMock.save(any())).thenAnswer(res -> res.getArgument(0));

        ItemRefundRequest request = ItemRefundRequest.builder()
            .productId(setupProduct.getId())
            .quantity(BigDecimal.valueOf(2L))
            .build();
        
        TransactionItemResponse transactionItem = transactionItemServiceMock.refundTransactionItem(setupTransactionItem.getId(), request);

        assertTrue(BigDecimal.valueOf(1000L).compareTo(transactionItem.price()) == 0);
        assertTrue(BigDecimal.valueOf(-2L).compareTo(transactionItem.quantity()) == 0); 
        assertTrue(BigDecimal.valueOf(3L).compareTo(setupProduct.getStockQuantity()) == 0);
        assertTrue(BigDecimal.valueOf(1000L).compareTo(setupTransaction.getTotalUnpaid()) == 0);
        assertTrue(BigDecimal.valueOf(0L).compareTo(setupCustomer.getTotalUnpaid()) == 0);
        assertEquals(TransactionStatus.COMPLETE, setupTransaction.getStatus());
        assertEquals("REFUND: " + setupProduct.getName(), transactionItem.description());

        verify(stockCardRepositoryMock, times(1)).save(argThat(arg -> 
            arg.getOldStock().compareTo(BigDecimal.valueOf(1L)) == 0 && 
            arg.getQuantity().compareTo(BigDecimal.valueOf(2L)) == 0 && 
            arg.getNewStock().compareTo(BigDecimal.valueOf(3L)) == 0 && 
            arg.getType() == StockMove.CUSTOMER_IN));
    }

    @Test
    @DisplayName("Should calcute Transaction total , return TransactionItemResponse DTO when refund action transaction item is successful CASE 2: PARTIAL REFUND, BOUGHT 3Q, REFUND 1Q")
    public void refundTransactionItem_validRequestCase2_returnTransactionItemResponse(){
        
        setupProduct.setSellPrice(BigDecimal.valueOf(1000L));
        setupProduct.setStockQuantity(BigDecimal.valueOf(1L));

        setupTransaction.setStatus(TransactionStatus.PROCESS);
        setupTransaction.setTotalUnpaid(BigDecimal.valueOf(700L));
        setupTransaction.setTotalPaid(BigDecimal.valueOf(1300L));

        setupCustomer.setTotalUnpaid(BigDecimal.valueOf(1200L));
        setupCustomer.setTotalPaid(BigDecimal.valueOf(1300L));
        setupTransaction.setCustomer(setupCustomer);

        setupTransactionItem.setPrice(BigDecimal.valueOf(1000L));
        setupTransactionItem.setQuantity(BigDecimal.valueOf(3L));
        setupTransactionItem.setTransaction(setupTransaction);
        setupTransactionItem.setProduct(setupProduct); 

        TransactionItem partialRefund = TransactionItem.builder()
            .quantity(BigDecimal.valueOf(-1L))
            .build();

        when(transactionItemRepositoryMock.findByTransactionIdAndProductId(any(), anyLong())).thenReturn(List.of(setupTransactionItem, partialRefund));
        when(transactionItemRepositoryMock.save(any())).thenAnswer(res -> res.getArgument(0));
        when(stockCardRepositoryMock.save(any())).thenAnswer(res -> res.getArgument(0));

        ItemRefundRequest request = ItemRefundRequest.builder()
            .productId(setupProduct.getId())
            .quantity(BigDecimal.valueOf(1L))
            .build();
        
        TransactionItemResponse transactionItem = transactionItemServiceMock.refundTransactionItem(setupTransactionItem.getId(), request);
 
        assertTrue(BigDecimal.valueOf(-1L).compareTo(transactionItem.quantity()) == 0); 
        assertTrue(BigDecimal.valueOf(2L).compareTo(setupProduct.getStockQuantity()) == 0);
        assertTrue(BigDecimal.valueOf(0L).compareTo(setupTransaction.getTotalUnpaid()) == 0);
        assertTrue(BigDecimal.valueOf(300L).compareTo(setupTransaction.getTotalUnrefunded()) == 0);
        assertTrue(BigDecimal.valueOf(500L).compareTo(setupCustomer.getTotalUnpaid()) == 0);
        assertTrue(BigDecimal.valueOf(1000L).compareTo(setupCustomer.getTotalPaid()) == 0);
        assertTrue(BigDecimal.valueOf(300L).compareTo(setupCustomer.getTotalUnrefunded()) == 0); 
        assertEquals("REFUND: " + setupProduct.getName(), transactionItem.description());

        verify(stockCardRepositoryMock, times(1)).save(argThat(arg -> 
            arg.getOldStock().compareTo(BigDecimal.valueOf(1L)) == 0 && 
            arg.getQuantity().compareTo(BigDecimal.valueOf(1L)) == 0 && 
            arg.getNewStock().compareTo(BigDecimal.valueOf(2L)) == 0 && 
            arg.getType() == StockMove.CUSTOMER_IN));
    }
    
    @Test
    @DisplayName("Should thrown not found error when transactionItem not found")
    public void refundTransactionItem_invalidTransactionId_throwNotFoundError(){ 
        when(transactionItemRepositoryMock.findByTransactionIdAndProductId(any(), anyLong())).thenReturn(List.of());

        ItemRefundRequest request = ItemRefundRequest.builder().productId(1L).build();
        assertThrows(NotFoundEntityException.class, () -> transactionItemServiceMock.refundTransactionItem(UuidCreator.getTimeOrderedEpochFast(), request));
    }

    @Test
    @DisplayName("Should thrown forbidden request error when refund quantity higher than actual")
    public void refundTransactionItem_refundMoreThanBought_throwForbiddenReqError(){ 
        setupTransactionItem.setQuantity(BigDecimal.valueOf(2L));
        when(transactionItemRepositoryMock.findByTransactionIdAndProductId(any(), anyLong())).thenReturn(List.of(setupTransactionItem));

        ItemRefundRequest request = ItemRefundRequest.builder()
            .productId(setupProduct.getId())
            .quantity(BigDecimal.valueOf(3L))
            .build();

        assertThrows(ForbiddenRequestException.class, () -> transactionItemServiceMock.refundTransactionItem(null, request));
    }

    @Test
    @DisplayName("Should thrown forbidden request error when refund quantity higher than actual CASE 2")
    public void refundTransactionItem_refundMoreThanLeft_throwForbiddenReqError(){ 
        setupTransactionItem.setQuantity(BigDecimal.valueOf(2L));

        TransactionItem transactionItem = TransactionItem.builder()
            .quantity(BigDecimal.valueOf(-2L))
            .build();

        when(transactionItemRepositoryMock.findByTransactionIdAndProductId(any(), anyLong())).thenReturn(List.of(setupTransactionItem, transactionItem));

        ItemRefundRequest request = ItemRefundRequest.builder()
            .productId(setupProduct.getId())
            .quantity(BigDecimal.valueOf(2L))
            .build();

        assertThrows(ForbiddenRequestException.class, () -> transactionItemServiceMock.refundTransactionItem(null, request));
    }
 
    @Test
    @DisplayName("Should thrown forbidden request error when transaction status other than process / complete ")
    public void refundTransactionItem_statusPending_throwForbiddenReqError(){ 
        setupTransaction.setStatus(TransactionStatus.PENDING);
        setupTransactionItem.setQuantity(BigDecimal.valueOf(3L));
        setupTransactionItem.setTransaction(setupTransaction);
        when(transactionItemRepositoryMock.findByTransactionIdAndProductId(any(), anyLong())).thenReturn(List.of(setupTransactionItem));

        ItemRefundRequest request = ItemRefundRequest.builder()
            .productId(setupProduct.getId())
            .quantity(BigDecimal.valueOf(3L))
            .build();
        
        assertThrows(ForbiddenRequestException.class, () -> transactionItemServiceMock.refundTransactionItem(null, request));
    }   

}