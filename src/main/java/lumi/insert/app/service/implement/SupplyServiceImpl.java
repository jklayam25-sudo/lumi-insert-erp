package lumi.insert.app.service.implement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import lumi.insert.app.core.entity.Supplier;
import lumi.insert.app.core.entity.Supply;
import lumi.insert.app.core.entity.SupplyItem;
import lumi.insert.app.core.entity.nondatabase.ActivityAction;
import lumi.insert.app.core.entity.nondatabase.StockMove;
import lumi.insert.app.core.entity.nondatabase.SupplyStatus;
import lumi.insert.app.core.repository.ProductRepository;
import lumi.insert.app.core.repository.StockCardRepository;
import lumi.insert.app.core.repository.SupplierRepository;
import lumi.insert.app.core.repository.SupplyItemRepository;
import lumi.insert.app.core.repository.SupplyRepository;
import lumi.insert.app.dto.request.ItemRefundRequest;
import lumi.insert.app.dto.request.SupplyCreateRequest;
import lumi.insert.app.dto.request.SupplyGetByFilter;
import lumi.insert.app.dto.request.SupplyItemCreate; 
import lumi.insert.app.dto.request.SupplyUpdateRequest;
import lumi.insert.app.dto.response.SupplyDetailResponse;
import lumi.insert.app.dto.response.SupplyResponse;
import lumi.insert.app.exception.ForbiddenRequestException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.TransactionValidationException;
import lumi.insert.app.mapper.AllSupplyMapper;
import lumi.insert.app.service.SupplyService;
import lumi.insert.app.utils.generator.JpaSpecGenerator;

@Service
@Transactional
@Slf4j
public class SupplyServiceImpl implements SupplyService{

    @Autowired
    SupplyRepository supplyRepository;

    @Autowired
    AllSupplyMapper allSupplyMapper;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    SupplyItemRepository supplyItemRepository;

    @Autowired
    StockCardRepository stockCardRepository;

    @Autowired
    JpaSpecGenerator jpaSpecGenerator;

    @Override
    @ActivityLogger(
        entityName = "supplies",
        action = ActivityAction.SUPPLY_ORDER_PLACED,
        actionMessage = "New supply order placed"
    )
    public SupplyResponse createSupply(SupplyCreateRequest request) {
        log.info("Creating supply order for supplier ID: {}", request.getSupplierId());
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
             .orElseThrow(() -> {
                 log.debug("Supplier not found for supply creation with ID: {}", request.getSupplierId());
                 return new NotFoundEntityException("Supplier with id " + request.getSupplierId() + " is not found");
             });

        List<SupplyItemCreate> items = request.getSupplyItems();
        log.debug("Supply creation request contains {} items", items.size());

        List<Long> listOfProductId = items.stream().map(item -> item.getProductId()).distinct().collect(Collectors.toCollection(ArrayList::new));
        List<Product> listOfProduct = productRepository.findAllById(listOfProductId);

        if(listOfProduct.size() != listOfProductId.size()){
            listOfProductId.removeAll(listOfProduct.stream().map(Product::getId).distinct().toList());
            log.debug("Supply creation failed - product ids not found: {}", listOfProductId);
            throw new NotFoundEntityException("Product with id " + listOfProductId.toString() + " not found!");
        } 

        BigDecimal subTotal = items.stream().map(item -> item.getPrice().multiply(item.getQuantity())).reduce(BigDecimal.ZERO, BigDecimal::add);
        log.debug("Supply subtotal calculated: {}", subTotal);

        Supply supply = Supply.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .invoiceId(request.getInvoiceId())
            .description(request.getDescription())
            .supplier(supplier)
            .supplierName(supplier.getName())
            .totalItems(Long.valueOf(items.size()))
            .subTotal(subTotal)
            .grandTotal(subTotal.subtract(request.getTotalDiscount()).add(request.getTotalFee()))
            .totalUnpaid(subTotal.subtract(request.getTotalDiscount()).add(request.getTotalFee()))
            .totalFee(request.getTotalFee())
            .totalDiscount(request.getTotalDiscount())
            .build();

        Supply savedSupply = supplyRepository.saveAndFlush(supply);

        Map<Long,Product> mappedProduct = listOfProduct.stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        List<SupplyItem> itemsToAdd = new ArrayList<>();

        List<StockCard> stockCardsToAdd = new ArrayList<>();

        for (SupplyItemCreate item : items) {
            Product product = mappedProduct.get(item.getProductId());
            
            BigDecimal oldPrice = product.getBasePrice();
            BigDecimal oldStock = product.getStockQuantity();

            SupplyItem supplyItem = SupplyItem.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .price(item.getPrice())
            .quantity(item.getQuantity())
            .description(item.getDescription())
            .product(product)
            .supply(savedSupply)
            .build();

            itemsToAdd.add(supplyItem);
            
            BigDecimal oldSubTotal = oldPrice.multiply(oldStock);
            BigDecimal supplySubTotal = supplyItem.getPrice().multiply(supplyItem.getQuantity());
            BigDecimal newStock = product.getStockQuantity().add(supplyItem.getQuantity());

            // set Product base price base on avg formula (total value from old stock + total value from supply divided by new stock)
            product.setBasePrice( 
                oldSubTotal
                    .add(supplySubTotal)
                    .divide(newStock, 4, RoundingMode.HALF_UP)
            );

            product.setStockQuantity(newStock);

            StockCard stockCard = StockCard.builder() 
                .id(UuidCreator.getTimeOrderedEpochFast())
                .referenceId(supplyItem.getId())
                .product(product)
                .productName(product.getName())
                .quantity(item.getQuantity())
                .oldStock(oldStock)
                .newStock(product.getStockQuantity())
                .type(StockMove.PURCHASE)
                .oldPrice(oldPrice)
                .newPrice(product.getBasePrice())
                .description("Product stock supply(IN)")
                .build();

            stockCardsToAdd.add(stockCard);
        }

        supplyItemRepository.saveAll(itemsToAdd);
        log.debug("Saved {} supply items", itemsToAdd.size());

        stockCardRepository.saveAll(stockCardsToAdd);
        log.debug("Saved {} stock cards", stockCardsToAdd.size());

        supplier.addTransaction();
        supplier.setTotalUnpaid(supplier.getTotalUnpaid().add(savedSupply.getTotalUnpaid()));
        SupplyResponse response = allSupplyMapper.createSimpleDTO(savedSupply);
        log.debug("Supply order created successfully: {}", response);
        return response;
    }

    @Override
    public Slice<SupplyResponse> searchSuppliesByRequests(SupplyGetByFilter request) {
        log.debug("Searching supplies with filter: {}", request);
        Pageable pageable = jpaSpecGenerator.pageable(request);
     
        Specification<Supply> supplySpecification = jpaSpecGenerator.supplySpecification(request);

        Slice<Supply> supplies = supplyRepository.findAll(supplySpecification, pageable);
        log.debug("Found {} supplies", supplies.getNumberOfElements());
        return supplies.map(allSupplyMapper::createSimpleDTO);
    }

    @Override
    @ActivityLogger(
        entityName = "supplies",
        action = ActivityAction.SUPPLY_ORDER_CANCELLED,
        actionMessage = "Supply order cancelled"
    )
    public SupplyResponse cancelSupply(UUID id) {
        log.info("Cancelling supply order with ID: {}", id);
        Supply supply = supplyRepository.findByIdDetail(id)
            .orElseThrow(() -> {
                log.debug("Supply not found for cancellation with ID: {}", id);
                return new NotFoundEntityException("Supply with ID " + id + " was not found");
            });

        if(supply.getStatus() == SupplyStatus.CANCELLED) {
            log.debug("Supply cancellation failed - already cancelled: {}", id);
            throw new ForbiddenRequestException("Unable to cancel supply because Supply Status is CANCELLED");
        }

        List<SupplyItem> supplyItems = supply.getSupplyItems();
        
        Map<Long, BigDecimal> listRefunded = supplyItems.stream()
            .filter(item -> item.getQuantity().compareTo(BigDecimal.ZERO) < 0)
            .collect(Collectors
                .groupingBy(
                    item -> item.getProduct().getId(), 
                    Collectors.reducing(BigDecimal.ZERO, SupplyItem::getQuantity, BigDecimal::add))
            );

        List<SupplyItem> reverseToAdd = new ArrayList<>();

        List<StockCard> stockCardToAdd = new ArrayList<>();
        
        for (SupplyItem item : supplyItems) {
            if(item.getQuantity().compareTo(BigDecimal.ZERO) < 0) { 
                continue;
            }; 

            Product product = item.getProduct();
            BigDecimal oldStock = product.getStockQuantity();
            BigDecimal oldPrice = product.getBasePrice();

            BigDecimal alreadyRefundedProduct = listRefunded.get(product.getId());
            alreadyRefundedProduct = alreadyRefundedProduct != null ? alreadyRefundedProduct : BigDecimal.ZERO;

            if(oldStock.compareTo((item.getQuantity().add(alreadyRefundedProduct))) < 0) throw new TransactionValidationException("Unable to cancel supply items, product with id " + product.getId() + " doesn't have enough stock to refund, stock left: " + product.getStockQuantity());
            
            SupplyItem reverseItem = SupplyItem.builder()
                .id(UuidCreator.getTimeOrderedEpochFast())
                .price(item.getPrice())
                .quantity(item.getQuantity().add(alreadyRefundedProduct).negate())
                .description("CANCELLED ")
                .product(item.getProduct())
                .supply(item.getSupply())
                .build();

            reverseToAdd.add(reverseItem);

            BigDecimal oldSubTotal = oldPrice.multiply(oldStock);
            BigDecimal supplySubTotal = reverseItem.getPrice().multiply(reverseItem.getQuantity());
            BigDecimal newStock = product.getStockQuantity().add(reverseItem.getQuantity());
 
            // Calculate Product average base price if stock is not 0 
            if(product.getStockQuantity().subtract(reverseItem.getQuantity().abs()).compareTo(BigDecimal.ZERO) != 0) {
                product.setBasePrice( 
                    oldSubTotal
                        .add(supplySubTotal)
                        .divide(newStock, 4, RoundingMode.HALF_UP)
                ); 
            }

            product.setStockQuantity(newStock);

            stockCardToAdd.add(StockCard.builder() 
                .id(UuidCreator.getTimeOrderedEpochFast())
                .referenceId(reverseItem.getId())
                .product(product)
                .productName(product.getName())
                .quantity(reverseItem.getQuantity())
                .oldStock(oldStock)
                .newStock(product.getStockQuantity())
                .type(StockMove.SUPPLIER_OUT)
                .oldPrice(oldPrice)
                .newPrice(product.getBasePrice())
                .description("Supply Cancelled, Product refunded. Status: CUSTOMER_OUT(OUT)")
                .build()
            );   
        }
        supplyItemRepository.saveAll(reverseToAdd);
        log.debug("Saved {} refund supply items", reverseToAdd.size());
        stockCardRepository.saveAll(stockCardToAdd);
        log.debug("Saved {} refund stock cards", stockCardToAdd.size());
        
        Supplier supplier = supply.getSupplier();
        supplier.setTotalUnpaid(supplier.getTotalUnpaid().subtract(supply.getTotalUnpaid()));
        supplier.setTotalPaid(supplier.getTotalPaid().subtract(supply.getTotalPaid()));
        supplier.setTotalUnrefunded(supplier.getTotalUnrefunded().add(supply.getTotalPaid()));

        supply.setStatus(SupplyStatus.CANCELLED);
        supply.setTotalUnrefunded(supply.getTotalPaid().add(supply.getTotalUnrefunded()));
        supply.setTotalUnpaid(BigDecimal.ZERO);
        supply.setTotalPaid(BigDecimal.ZERO); 

        SupplyResponse response = allSupplyMapper.createSimpleDTO(supply);
        log.debug("Supply cancellation response created: {}", response);
        return response;
    }

    @Override
    public SupplyDetailResponse getSupply(UUID id) {
        log.debug("Getting supply detail for ID: {}", id);
        Supply supply = supplyRepository.findByIdDetail(id)
            .orElseThrow(() -> {
                log.debug("Supply not found with ID: {}", id);
                return new NotFoundEntityException("Supply with ID " + id + " is not found");
            });

        SupplyDetailResponse response = allSupplyMapper.createDetailDTO(supply);
        log.debug("Supply detail response created: {}", response);
        return response;
    }

    @Override
    @ActivityLogger(
        entityName = "supplies",
        action = ActivityAction.SUPPLY_ORDER_UPDATED,
        actionMessage = "Supply updated"
    )
    public SupplyResponse updateSupply(UUID id, SupplyUpdateRequest request) {
        log.info("Updating supply order with ID: {}", id);
        Supply supply = supplyRepository.findByIdDetail(id)
            .orElseThrow(() -> {
                log.debug("Supply not found for update with ID: {}", id);
                return new NotFoundEntityException("Supply with ID " + id + " is not found");
            });
 
        if(supply.getStatus() == SupplyStatus.CANCELLED) {
            log.debug("Supply update failed - supply already cancelled: {}", id);
            throw new ForbiddenRequestException("Unable to cancel supply because Supply Status is CANCELLED");
        }

        allSupplyMapper.updateSupply(request, supply);

        if(request.getTotalDiscount() == null) request.setTotalDiscount(BigDecimal.ZERO);
        if(request.getTotalFee() == null) request.setTotalFee(BigDecimal.ZERO);
        if(request.getTotalDiscount().compareTo(BigDecimal.ZERO) != 0 || request.getTotalFee().compareTo(BigDecimal.ZERO) != 0){
            log.debug("Updating supply totals for ID: {}, discount: {}, fee: {}", id, request.getTotalDiscount(), request.getTotalFee());
            BigDecimal totalChange = request.getTotalDiscount().subtract(request.getTotalFee());
            BigDecimal oldTotalUnpaid = supply.getTotalUnpaid();
            BigDecimal oldTotalPaid = supply.getTotalPaid();
            BigDecimal oldTotalUnrefunded = supply.getTotalUnrefunded();

            supply.setGrandTotal(supply.getSubTotal().subtract(totalChange));
            
            // Possible to have negate value
            supply.setTotalUnpaid(oldTotalUnpaid.subtract(totalChange));
            BigDecimal changeTotalUnpaid = supply.getTotalUnpaid();

            // Calculate if unpaid is negate(surplus) > allocate surplus to unrefunded 
            if(changeTotalUnpaid.compareTo(BigDecimal.ZERO) < 0){
                supply.setTotalUnpaid(BigDecimal.ZERO);
                supply.setTotalPaid(oldTotalPaid.subtract(changeTotalUnpaid.abs()));
                supply.setTotalUnrefunded(oldTotalUnrefunded.add(changeTotalUnpaid.abs()));
           }

           if(supply.getTotalUnpaid().compareTo(BigDecimal.ZERO) == 0) supply.setStatus(SupplyStatus.COMPLETE);

           // Calculate supplier payment detail
           Supplier supplier = supply.getSupplier();

           BigDecimal deltaUnpaid = oldTotalUnpaid.subtract(supply.getTotalUnpaid());
           BigDecimal deltaPaid = oldTotalPaid.subtract(supply.getTotalPaid());
           BigDecimal deltaUnrefunded = oldTotalUnrefunded.subtract(supply.getTotalUnrefunded());

           supplier.setTotalUnpaid(supplier.getTotalUnpaid().subtract(deltaUnpaid));
           supplier.setTotalPaid(supplier.getTotalPaid().subtract(deltaPaid));
           supplier.setTotalUnrefunded(supplier.getTotalUnrefunded().subtract(deltaUnrefunded));
        }

        return allSupplyMapper.createSimpleDTO(supply);
    }

    @Override
    @ActivityLogger(
        entityName = "supplies",
        action = ActivityAction.SUPPLY_ITEM_REFUNDED,
        actionMessage = "Supply item refunded"
    )
    public SupplyResponse refundSupplyItem(UUID id, ItemRefundRequest request) { 
        log.info("Refunding supply item for supply ID: {}, product ID: {}, quantity: {}", id, request.getProductId(), request.getQuantity());

        List<SupplyItem> matchItems = supplyItemRepository.findBySupplyIdAndProductId(id, request.getProductId());
        if(matchItems.size() == 0) {
            log.debug("No supply items found for refund request supply ID: {}, product ID: {}", id, request.getProductId());
            throw new NotFoundEntityException("Unable to find any supply item with product id " + request.getProductId());
        }
        BigDecimal priceFromSupplier = matchItems.getLast().getPrice();
        BigDecimal ttlQuantiyLeft = matchItems.stream()
            .map(item -> item.getQuantity())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if(ttlQuantiyLeft.compareTo(request.getQuantity()) < 0) {
            log.debug("Refund request quantity {} exceeds remaining refundable stock {} for supply ID: {}", request.getQuantity(), ttlQuantiyLeft, id);
            throw new ForbiddenRequestException("refund quantity exceeds the remaining refundedable stock with quantity: " + ttlQuantiyLeft + ", enter an exact amount to proceed");
        }

        Supply supply = matchItems.getFirst().getSupply();

        if(supply.getStatus() == SupplyStatus.CANCELLED) {
            log.debug("Refund request failed - supply already cancelled: {}", id);
            throw new ForbiddenRequestException("Unable to cancel supply because Supply Status is CANCELLED");
        }

        Product product = matchItems.getFirst().getProduct();

        if(product.getStockQuantity().compareTo(request.getQuantity()) < 0 ) {
            log.debug("Refund request failed - insufficient stock for product ID: {}. requested {}, available {}", product.getId(), request.getQuantity(), product.getStockQuantity());
            throw new TransactionValidationException("Unable to cancel supply items, product with id " + product.getId() + " doesn't have enough stock to refund, stock left: " + product.getStockQuantity());
        }

        SupplyItem supplyItem = SupplyItem.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .price(priceFromSupplier)
            .quantity(request.getQuantity().negate())
            .description("REFUNDED ")
            .product(product)
            .supply(supply)
            .build();

        BigDecimal oldPrice = product.getBasePrice();
        BigDecimal oldStock = product.getStockQuantity();

        BigDecimal oldSubTotal = oldPrice.multiply(oldStock);
        BigDecimal supplySubTotal = priceFromSupplier.multiply(request.getQuantity());
        BigDecimal newStock = product.getStockQuantity().add(supplyItem.getQuantity());
 
        // Calculate Product average base price if stock is not 0 
        if(product.getStockQuantity().subtract(request.getQuantity().abs()).compareTo(BigDecimal.ZERO) != 0) {
            product.setBasePrice( 
                oldSubTotal
                    .subtract(supplySubTotal)
                    .divide(newStock, 4, RoundingMode.HALF_UP)
            ); 
        } 

        product.setStockQuantity(oldStock.subtract(request.getQuantity())); 
 
        BigDecimal oldTotalUnpaid = supply.getTotalUnpaid();
        BigDecimal oldTotalPaid = supply.getTotalPaid();
        BigDecimal oldTotalUnrefunded = supply.getTotalUnrefunded(); 

        StockCard stockCard = StockCard.builder() 
            .id(UuidCreator.getTimeOrderedEpochFast())
            .referenceId(supplyItem.getId())
            .product(product)
            .productName(product.getName())
            .quantity(supplyItem.getQuantity())
            .oldStock(oldStock)
            .newStock(product.getStockQuantity())
            .type(StockMove.SUPPLIER_OUT)
            .oldPrice(oldPrice)
            .newPrice(product.getBasePrice())
            .description("Supply Cancelled, Product refunded. Status: SUPPLIER_OUT(OUT)")
            .build();

        BigDecimal totalChange = priceFromSupplier.multiply(request.getQuantity());

        // Possible to have negate value
        supply.setTotalUnpaid(supply.getTotalUnpaid().subtract(totalChange));
        BigDecimal changeTotalUnpaid = supply.getTotalUnpaid();
        
        // Calculate if unpaid is negate(surplus) > allocate surplus to unrefunded 
        if(changeTotalUnpaid.compareTo(BigDecimal.ZERO) < 0){
            supply.setTotalUnpaid(BigDecimal.ZERO);
            supply.setTotalPaid(oldTotalPaid.subtract(changeTotalUnpaid.abs()));
            supply.setTotalUnrefunded(oldTotalUnrefunded.add(changeTotalUnpaid.abs()));
        }

        if(supply.getTotalUnpaid().compareTo(BigDecimal.ZERO) == 0) supply.setStatus(SupplyStatus.COMPLETE);

        // Calculate supplier payment detail
        Supplier supplier = supply.getSupplier();

        BigDecimal deltaUnpaid = oldTotalUnpaid.subtract(supply.getTotalUnpaid());
        BigDecimal deltaPaid = oldTotalPaid.subtract(supply.getTotalPaid());
        BigDecimal deltaUnrefunded = oldTotalUnrefunded.subtract(supply.getTotalUnrefunded());

        supplier.setTotalUnpaid(supplier.getTotalUnpaid().subtract(deltaUnpaid));
        supplier.setTotalPaid(supplier.getTotalPaid().subtract(deltaPaid));
        supplier.setTotalUnrefunded(supplier.getTotalUnrefunded().subtract(deltaUnrefunded));

        stockCardRepository.save(stockCard);
        supplyItemRepository.save(supplyItem);
        SupplyResponse response = allSupplyMapper.createSimpleDTO(supply);
        log.debug("Supply refund response created: {}", response);
        return response;
    }
    
}
