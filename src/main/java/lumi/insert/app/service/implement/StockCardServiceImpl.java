package lumi.insert.app.service.implement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.aspect.annotation.ActivityLogger;
import lumi.insert.app.core.entity.Product;
import lumi.insert.app.core.entity.StockCard;
import lumi.insert.app.core.entity.nondatabase.ActivityAction;
import lumi.insert.app.core.entity.nondatabase.StockMove;
import lumi.insert.app.core.repository.ProductRepository;
import lumi.insert.app.core.repository.StockCardRepository;
import lumi.insert.app.core.repository.TransactionItemRepository;
import lumi.insert.app.dto.request.PaginationRequest;
import lumi.insert.app.dto.request.StockCardCreateRequest;
import lumi.insert.app.dto.request.StockCardGetByFilter;
import lumi.insert.app.dto.response.StockCardResponse;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.TransactionValidationException;
import lumi.insert.app.mapper.StockCardMapper;
import lumi.insert.app.service.StockCardService;
import lumi.insert.app.utils.generator.JpaSpecGenerator;

@Service
@Transactional
@Slf4j
public class StockCardServiceImpl implements StockCardService{

    @Autowired
    StockCardRepository stockCardRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    TransactionItemRepository transactionItemRepository;

    @Autowired
    StockCardMapper stockCardMapper;

    @Autowired
    JpaSpecGenerator jpaSpecGenerator;

    @Override
    @ActivityLogger(
        entityName = "stock_cards",
        action = ActivityAction.STOCK_ADJUSTMENT,
        actionMessage = "Product stock adjustment"
    )
    public StockCardResponse createStockCard(StockCardCreateRequest request) {
        log.info("Creating stock card for productId={}, referenceId={}, type={}", request.getProductId(), request.getReferenceId(), request.getType());
        if((request.getType() == StockMove.CUSTOMER_IN.toString() || request.getType() == StockMove.SUPPLIER_IN.toString() ||
            request.getType() == StockMove.REPAIRED.toString()) && request.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
                log.debug("Invalid stock card quantity for IN type, quantity={}", request.getQuantity());
                throw new TransactionValidationException("Stock 'IN' type should be positive quantity");
        }

        if((request.getType() == StockMove.CUSTOMER_OUT.toString() || request.getType() == StockMove.SUPPLIER_OUT.toString() ||
            request.getType() == StockMove.DEFECT.toString()) && request.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                log.debug("Invalid stock card quantity for OUT type, quantity={}", request.getQuantity());
                throw new TransactionValidationException("Stock 'OUT' type should be negative quantity");
        }

        if((request.getType() == StockMove.CUSTOMER_IN.toString() || request.getType() == StockMove.CUSTOMER_OUT.toString()) && !transactionItemRepository.existsById(request.getReferenceId())) {
            log.debug("Stock card creation failed, transaction item not found referenceId={}", request.getReferenceId());
            throw new NotFoundEntityException("Transaction Items with ID " + request.getReferenceId() + " was not found");
        }

        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new NotFoundEntityException("Product with ID " + request.getProductId() + " was not found"));

        BigDecimal oldStock = product.getStockQuantity();

        product.setStockQuantity(oldStock.add(request.getQuantity()));
 
        if(product.getStockQuantity().compareTo(BigDecimal.ZERO) < 0) throw new TransactionValidationException("Product stocks with ID " + request.getProductId() + " doesn't meet buyer quantity, stock left: " + oldStock);

        StockCard stockCard = StockCard.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .referenceId(request.getReferenceId())
            .product(product)
            .productName(product.getName())
            .quantity(request.getQuantity())
            .oldStock(oldStock)
            .newStock(product.getStockQuantity())
            .type(StockMove.valueOf(request.getType()))
            .oldPrice(product.getBasePrice())
            .newPrice(product.getBasePrice())
            .build();

        StockCard savedStockCard = stockCardRepository.save(stockCard);
        
        StockCardResponse stockCardResponse = stockCardMapper.createDtoResponseFromStockCard(savedStockCard); 
        log.info("Stock card created stockCardId={} productId={}", savedStockCard.getId(), savedStockCard.getProduct().getId());
        return stockCardResponse;
    }

    @Override
    public StockCardResponse getStockCard(UUID id) {
        log.info("Retrieving stock card id={}", id);
        StockCard stockCard = stockCardRepository.findById(id)
            .orElseThrow(() -> {
                log.debug("Stock card not found id={}", id);
                return new NotFoundEntityException("StockCard with ID " + id + " was not found");
            });

        return stockCardMapper.createDtoResponseFromStockCard(stockCard);
    }

    @Override
    public Slice<StockCardResponse> getStockCards(UUID lastId, PaginationRequest request) {
        if(lastId != null) request.setPage(0);
        Pageable pageable = jpaSpecGenerator.pageable(request);

        Slice<StockCardResponse> slices = stockCardRepository.findByIndexPagination(LocalDateTime.of(1900, 10, 10, 10, 10), LocalDateTime.of(3000, 10, 10, 10, 10), lastId, pageable);

        return slices;
    }
 
    @Override
    public Slice<StockCardResponse> searchStockCards(StockCardGetByFilter request) {
        Pageable pageable = jpaSpecGenerator.pageable(request);

        Specification<StockCard> stockCardSpecification = jpaSpecGenerator.stockCardSpecification(request);

        Slice<StockCard> slices = stockCardRepository.findAll(stockCardSpecification, pageable);

        return slices.map(stockCardMapper::createDtoResponseFromStockCard);
    }
    
}
