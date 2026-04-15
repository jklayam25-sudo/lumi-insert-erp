package lumi.insert.app.service.stockcard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import lumi.insert.app.core.entity.StockCard;
import lumi.insert.app.core.entity.nondatabase.StockMove;
import lumi.insert.app.dto.request.StockCardCreateRequest;
import lumi.insert.app.dto.response.StockCardResponse;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.TransactionValidationException;

public class StockCardCreateServiceTest extends BaseStockCardServiceTest {

    @Test
    void createStockCard_validRequest_returnDTO() {
        when(transactionItemRepository.existsById(any(UUID.class))).thenReturn(true);

        when(productRepository.findById(setupProduct.getId())).thenReturn(Optional.of(setupProduct));

        when(stockCardRepository.save(any(StockCard.class))).thenAnswer(arg -> arg.getArgument(0));

        StockCardCreateRequest request = StockCardCreateRequest.builder()
                .referenceId(setupTransactionItem.getId())
                .productId(setupProduct.getId())
                .quantity(BigDecimal.valueOf(-5L))
                .type("CUSTOMER_OUT")
                .description("Return broken product from TEST LTE.")
                .build();

        StockCardResponse stockCardRes = stockCardService.createStockCard(request);

        assertEquals(setupProduct.getBasePrice(), stockCardRes.newPrice());
        assertTrue(BigDecimal.valueOf(10L).compareTo(stockCardRes.oldStock()) == 0);
        assertEquals(setupProduct.getName(), stockCardRes.productName());
        assertTrue(BigDecimal.valueOf(5L).compareTo(stockCardRes.newStock()) == 0);
        assertEquals(StockMove.CUSTOMER_OUT, stockCardRes.type());
    }

    @Test
    void createStockCard_missmatchQuantity_throwTransactionValidation() {

        StockCardCreateRequest request = StockCardCreateRequest.builder()
                .referenceId(setupTransactionItem.getId())
                .productId(setupProduct.getId())
                .quantity(BigDecimal.valueOf(5L))
                .type("CUSTOMER_OUT")
                .description("Return broken product from TEST LTE.")
                .build();

        assertThrows(TransactionValidationException.class, () -> stockCardService.createStockCard(request));
    }

    @Test
    void createStockCard_missmatchQuantity2_throwTransactionValidation() {

        StockCardCreateRequest request = StockCardCreateRequest.builder()
                .referenceId(setupTransactionItem.getId())
                .productId(setupProduct.getId())
                .quantity(BigDecimal.valueOf(-5L))
                .type("CUSTOMER_IN")
                .description("Return broken product from TEST LTE.")
                .build();

        assertThrows(TransactionValidationException.class, () -> stockCardService.createStockCard(request));
    }

    @Test
    void createStockCard_notFoundReference_throwNotFoundEntityException() {
        when(transactionItemRepository.existsById(any(UUID.class))).thenReturn(false);

        StockCardCreateRequest request = StockCardCreateRequest.builder()
                .referenceId(setupTransactionItem.getId())
                .productId(setupProduct.getId())
                .quantity(BigDecimal.valueOf(-5L))
                .type("CUSTOMER_OUT")
                .description("Return broken product from TEST LTE.")
                .build();

        assertThrows(NotFoundEntityException.class, () -> stockCardService.createStockCard(request));

        verify(transactionItemRepository, times(1)).existsById(setupTransactionItem.getId());
    }

    @Test
    void createStockCard_notFoundProduct_throwNotFoundEntityException() {
        when(transactionItemRepository.existsById(any(UUID.class))).thenReturn(true);

        when(productRepository.findById(setupProduct.getId())).thenReturn(Optional.empty());

        StockCardCreateRequest request = StockCardCreateRequest.builder()
                .referenceId(setupTransactionItem.getId())
                .productId(setupProduct.getId())
                .quantity(BigDecimal.valueOf(-5L))
                .type("CUSTOMER_OUT")
                .description("Return broken product from TEST LTE.")
                .build();

        assertThrows(NotFoundEntityException.class, () -> stockCardService.createStockCard(request));

        verify(productRepository, times(1)).findById(setupProduct.getId());
    }

    @Test
    void createStockCard_productStockMinus_throwTransactionValidationException() {
        when(transactionItemRepository.existsById(any(UUID.class))).thenReturn(true);

        when(productRepository.findById(setupProduct.getId())).thenReturn(Optional.of(setupProduct));

        StockCardCreateRequest request = StockCardCreateRequest.builder()
                .referenceId(setupTransactionItem.getId())
                .productId(setupProduct.getId())
                .quantity(BigDecimal.valueOf(-11L))
                .type("CUSTOMER_OUT")
                .description("Return broken product from TEST LTE.")
                .build();

        assertThrows(TransactionValidationException.class, () -> stockCardService.createStockCard(request));

    }
}