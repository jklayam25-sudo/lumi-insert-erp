package lumi.insert.app.service.transactionitem;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import lumi.insert.app.core.entity.nondatabase.TransactionStatus;
import lumi.insert.app.dto.response.TransactionItemDelete;
import lumi.insert.app.exception.ForbiddenRequestException;
import lumi.insert.app.exception.NotFoundEntityException; 

public class TransactionItemServiceDeleteTest extends BaseTransactionItemServiceTest{
    
    @Test
    @DisplayName("Should calcute Transaction total and delete entity, return TransactionItemDeleteResponse DTO when delete transaction item is successful")
    public void deleteTransactionItem_validRequest_returnTransactionItemResponse(){  
        setupTransaction.setTotalItems(1L);
        setupTransaction.setSubTotal(BigDecimal.valueOf(100000L));
        setupTransactionItem.setTransaction(setupTransaction);
        setupTransactionItem.setPrice(BigDecimal.valueOf(50000L));
        setupTransactionItem.setQuantity(BigDecimal.valueOf(1L));

        when(transactionItemRepositoryMock.findById(setupTransactionItem.getId())).thenReturn(Optional.of(setupTransactionItem));

        TransactionItemDelete deleteTransactionItem = transactionItemServiceMock.deleteTransactionItem(setupTransactionItem.getId());

        assertEquals(setupTransactionItem.getId(), deleteTransactionItem.id()); 
        assertTrue(deleteTransactionItem.deleted()); 
        assertTrue(BigDecimal.valueOf(50000L).compareTo(setupTransaction.getSubTotal()) == 0); 
        assertEquals(0L, setupTransaction.getTotalItems()); 

        verify(transactionItemRepositoryMock, times(1)).delete(setupTransactionItem);
    }

    @Test
    @DisplayName("Should thrown not found error when transaction item not found")
    public void deleteTransactionItem_invalidId_throwNotFoundError(){ 
        when(transactionItemRepositoryMock.findById(any())).thenReturn(Optional.empty()); 

        assertThrows(NotFoundEntityException.class, () -> transactionItemServiceMock.deleteTransactionItem(null));
    }

    @Test
    @DisplayName("Should thrown forbidden request error when transaction status is not PENDING")
    public void deleteTransactionItem_notPending_throwForbiddenReqError(){ 
        setupTransaction.setStatus(TransactionStatus.CANCELLED);
        setupTransactionItem.setTransaction(setupTransaction);

        when(transactionItemRepositoryMock.findById(any())).thenReturn(Optional.of(setupTransactionItem)); 

        assertThrows(ForbiddenRequestException.class, () -> transactionItemServiceMock.deleteTransactionItem(null));
    }

}
