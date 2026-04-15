package lumi.insert.app.service.transactionitem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any; 
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lumi.insert.app.core.entity.nondatabase.TransactionStatus;
import lumi.insert.app.dto.response.TransactionItemResponse;
import lumi.insert.app.exception.ForbiddenRequestException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.TransactionValidationException;

public class TransactionItemServiceEditTest extends BaseTransactionItemServiceTest {
    
    @Test
    @DisplayName("Should calculate Transaction total and update entity, return TransactionItemResponse DTO when updating transaction item is successful")
    public void updateTransactionItemQuantity_validRequest_returnTransactionItemResponse(){  
        // Mocking setup with BigDecimal
        setupTransaction.setTotalItems(1L);
        setupTransaction.setSubTotal(BigDecimal.valueOf(100000L));
        setupTransaction.setStatus(TransactionStatus.PENDING);
        setupTransactionItem.setTransaction(setupTransaction);

        setupProduct.setSellPrice(BigDecimal.valueOf(55000L));
        setupProduct.setStockQuantity(BigDecimal.valueOf(3L));
        setupTransactionItem.setProduct(setupProduct);
        
        setupTransactionItem.setPrice(BigDecimal.valueOf(50000L));
        setupTransactionItem.setQuantity(BigDecimal.valueOf(1L));

        when(transactionItemRepositoryMock.findById(setupTransactionItem.getId())).thenReturn(Optional.of(setupTransactionItem));

        // Act - Passing BigDecimal to the service method
        TransactionItemResponse updateTransactionItem = transactionItemServiceMock.updateTransactionItemQuantity(
            setupTransactionItem.getId(), 
            BigDecimal.valueOf(2L)
        );

        // Assert - Comparing BigDecimal values
        assertEquals(setupTransactionItem.getId(), updateTransactionItem.id());   
        assertEquals(160000L, setupTransaction.getSubTotal().longValue());
        assertTrue(BigDecimal.valueOf(160000L).compareTo(setupTransaction.getSubTotal()) == 0);
        assertTrue(BigDecimal.valueOf(2L).compareTo(setupTransactionItem.getQuantity()) == 0);
        assertTrue(BigDecimal.valueOf(55000L).compareTo(setupTransactionItem.getPrice()) == 0);
    }

    @Test
    @DisplayName("Should thrown not found error when transaction item not found")
    public void updateTransactionItemQuantity_invalidId_throwNotFoundError(){ 
        when(transactionItemRepositoryMock.findById(any())).thenReturn(Optional.empty()); 

        assertThrows(NotFoundEntityException.class, () -> transactionItemServiceMock.updateTransactionItemQuantity(null, BigDecimal.valueOf(1L)));
    }

    @Test
    @DisplayName("Should thrown forbidden request error when transaction status is not PENDING")
    public void updateTransactionItemQuantity_notPending_throwForbiddenReqError(){ 
        setupTransaction.setStatus(TransactionStatus.CANCELLED);
        setupTransactionItem.setTransaction(setupTransaction);

        when(transactionItemRepositoryMock.findById(any())).thenReturn(Optional.of(setupTransactionItem)); 

        assertThrows(ForbiddenRequestException.class, () -> transactionItemServiceMock.updateTransactionItemQuantity(setupTransactionItem.getId(), BigDecimal.valueOf(1L)));
    }

    @Test
    @DisplayName("Should thrown transactionValidate error when product stock lesser than request buy stock")
    public void updateTransactionItemQuantity_outOfStock_throwTransactionValidationExc(){  
        setupTransaction.setTotalItems(1L);
        setupTransaction.setSubTotal(BigDecimal.valueOf(100000L));
        setupTransaction.setStatus(TransactionStatus.PENDING);
        setupTransactionItem.setTransaction(setupTransaction);

        setupProduct.setSellPrice(BigDecimal.valueOf(55000L));
        setupProduct.setStockQuantity(BigDecimal.valueOf(3L));
        setupTransactionItem.setProduct(setupProduct);
        
        setupTransactionItem.setPrice(BigDecimal.valueOf(50000L));
        setupTransactionItem.setQuantity(BigDecimal.valueOf(1L));

        when(transactionItemRepositoryMock.findById(setupTransactionItem.getId())).thenReturn(Optional.of(setupTransactionItem));

        // Asserting out of stock scenario with BigDecimal
        assertThrows(TransactionValidationException.class, () -> 
            transactionItemServiceMock.updateTransactionItemQuantity(setupTransactionItem.getId(), BigDecimal.valueOf(999L))
        );
    }
}