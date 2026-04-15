package lumi.insert.app.service.stockcard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

import org.junit.jupiter.api.Test; 
import org.mockito.ArgumentMatchers; 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;

import lumi.insert.app.core.entity.StockCard;
import lumi.insert.app.core.entity.nondatabase.StockMove;
import lumi.insert.app.dto.request.PaginationRequest;
import lumi.insert.app.dto.request.StockCardGetByFilter;
import lumi.insert.app.dto.response.StockCardResponse;
import lumi.insert.app.exception.NotFoundEntityException;

public class StockCardGetServiceTest extends BaseStockCardServiceTest{
    
    @Test
    void getStockCard_foundEntity_returnDTO(){
        StockCard stockCard = StockCard.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .referenceId(setupTransactionItem.getId())
        .product(setupProduct)
        .productName(setupProduct.getName())
        .quantity(BigDecimal.valueOf(-5L))
        .oldStock(BigDecimal.valueOf(10L))
        .newStock(BigDecimal.valueOf(5L))
        .type(StockMove.SALE)
        .oldPrice(BigDecimal.valueOf(1000L))
        .newPrice(BigDecimal.valueOf(1000L))
        .build();

        when(stockCardRepository.findById(any(UUID.class))).thenReturn(Optional.of(stockCard));

        StockCardResponse response = stockCardService.getStockCard(stockCard.getId());

        assertEquals(stockCard.getOldPrice(), response.oldPrice());
        assertEquals(stockCard.getOldStock(), response.oldStock());
        assertEquals(stockCard.getProductName(), response.productName());
        assertEquals(stockCard.getNewStock(), response.newStock());
        assertEquals(stockCard.getType(), response.type());
    }

    @Test
    void getStockCard_notFoundEntity_throwNotFoundEntityException(){ 
        when(stockCardRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(NotFoundEntityException.class, () -> stockCardService.getStockCard(UuidCreator.getTimeOrderedEpochFast())); 
    }

    @Test
    void getStockCards_foundEntity_returnSliceDTO(){ 
        StockCardResponse stockCardResponse = StockCardResponse.builder().productName("Test").build();
        Slice<StockCardResponse> slices = new SliceImpl<>(List.of(stockCardResponse));

        when(stockCardRepository.findByIndexPagination(any(LocalDateTime.class), any(LocalDateTime.class), any(UUID.class), any(Pageable.class))).thenReturn(slices);

       Slice<StockCardResponse> stockCards = stockCardService.getStockCards(setupTransactionItem.getId(), PaginationRequest.builder().build());
       assertEquals(1, stockCards.getNumberOfElements());
       assertEquals("Test", stockCards.getContent().getFirst().productName());
    }

    @Test
    void getStockCards_notFoundEntity_returnEmptySliceDTO(){  
        Slice<StockCardResponse> slices = new SliceImpl<>(List.of( ));

        when(stockCardRepository.findByIndexPagination(any(LocalDateTime.class), any(LocalDateTime.class), any(UUID.class), any(Pageable.class))).thenReturn(slices);

       Slice<StockCardResponse> stockCards = stockCardService.getStockCards(setupTransactionItem.getId(), PaginationRequest.builder().build());
       assertEquals(0, stockCards.getNumberOfElements());
       assertEquals(List.of(), stockCards.getContent());
    }

    @Test
    void searchStockCards_foundEntity_returnSliceDTO(){  
        StockCard stockCard = StockCard.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .referenceId(setupTransactionItem.getId())
        .product(setupProduct)
        .productName(setupProduct.getName())
        .quantity(BigDecimal.valueOf(-5L))
        .oldStock(BigDecimal.valueOf(10L))
        .newStock(BigDecimal.valueOf(5L))
        .type(StockMove.SALE)
        .oldPrice(BigDecimal.valueOf(1000L))
        .newPrice(BigDecimal.valueOf(1000L))
        .build();

        Page<StockCard> slices = new PageImpl<>(List.of(stockCard));
        when(stockCardRepository.findAll(ArgumentMatchers.<Specification<StockCard>>any(), any(Pageable.class))).thenReturn(slices);

        StockCardGetByFilter request = StockCardGetByFilter.builder()
        .type(StockMove.SALE)
        .build();

        Slice<StockCardResponse> stockCards = stockCardService.searchStockCards(request);
        assertEquals(1, stockCards.getNumberOfElements());
        assertEquals(setupProduct.getName(), stockCards.getContent().getFirst().productName());

        verify(jpaSpecGenerator, times(1)).stockCardSpecification(argThat(arg -> arg.getType() == StockMove.SALE));
    }

    @Test
    void searchStockCards_notFoundEntity_returnSliceDTO(){   

        Page<StockCard> slices = new PageImpl<>(List.of());
        when(stockCardRepository.findAll(ArgumentMatchers.<Specification<StockCard>>any(), any(Pageable.class))).thenReturn(slices);

        StockCardGetByFilter request = StockCardGetByFilter.builder()
        .type(StockMove.SALE)
        .build();

        Slice<StockCardResponse> stockCards = stockCardService.searchStockCards(request);
        assertEquals(0, stockCards.getNumberOfElements());
        assertEquals(List.of(), stockCards.getContent());
 
    }


        
}
