package lumi.insert.app.service.implement;

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

        Long subTotal = items.stream().mapToLong(item -> item.getPrice() * item.getQuantity()).sum();
        log.debug("Supply subtotal calculated: {}", subTotal);

        Supply supply = Supply.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .invoiceId(request.getInvoiceId())
            .description(request.getDescription())
            .supplier(supplier)
            .supplierName(supplier.getName())
            .totalItems(Long.valueOf(items.size()))
            .subTotal(subTotal)
            .grandTotal(subTotal - request.getTotalDiscount() + request.getTotalFee())
            .totalUnpaid(subTotal - request.getTotalDiscount() + request.getTotalFee())
            .totalFee(request.getTotalFee())
            .totalDiscount(request.getTotalDiscount())
            .build();

        Supply savedSupply = supplyRepository.saveAndFlush(supply);

        Map<Long,Product> mappedProduct = listOfProduct.stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        List<SupplyItem> itemsToAdd = new ArrayList<>();

        List<StockCard> stockCardsToAdd = new ArrayList<>();

        for (SupplyItemCreate item : items) {
            Product product = mappedProduct.get(item.getProductId());
            
            Long oldPrice = product.getBasePrice();
            Long oldStock = product.getStockQuantity();

            SupplyItem supplyItem = SupplyItem.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .price(item.getPrice())
            .quantity(item.getQuantity())
            .description(item.getDescription())
            .product(product)
            .supply(savedSupply)
            .build();

            itemsToAdd.add(supplyItem);
            
            product.setBasePrice(((oldPrice * product.getStockQuantity()) + (supplyItem.getPrice() * supplyItem.getQuantity())) / (product.getStockQuantity() + supplyItem.getQuantity()));
            product.setStockQuantity(product.getStockQuantity() + supplyItem.getQuantity());

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
        supplier.setTotalUnpaid(supplier.getTotalUnpaid() + savedSupply.getTotalUnpaid());
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
        
        Map<Long, Long> listRefunded = supplyItems.stream()
            .filter(item -> item.getQuantity() < 0)
            .collect(Collectors
                .groupingBy(item -> item.getProduct().getId(), Collectors.summingLong(item -> item.getQuantity()))
            );

        List<SupplyItem> reverseToAdd = new ArrayList<>();

        List<StockCard> stockCardToAdd = new ArrayList<>();
        
        for (SupplyItem item : supplyItems) {
            if(item.getQuantity() < 0) { 
                continue;
            }; 

            Product product = item.getProduct();
            Long oldStock = product.getStockQuantity();
            Long oldPrice = product.getBasePrice();

            Long alreadyRefundedProduct = listRefunded.get(product.getId());
            alreadyRefundedProduct = alreadyRefundedProduct != null ? alreadyRefundedProduct : 0L;

            if(oldStock < (item.getQuantity() + alreadyRefundedProduct)) throw new TransactionValidationException("Unable to cancel supply items, product with id " + product.getId() + " doesn't have enough stock to refund, stock left: " + product.getStockQuantity());
            
            SupplyItem reverseItem = SupplyItem.builder()
                .id(UuidCreator.getTimeOrderedEpochFast())
                .price(item.getPrice())
                .quantity(-(item.getQuantity() + alreadyRefundedProduct))
                .description("CANCELLED ")
                .product(item.getProduct())
                .supply(item.getSupply())
                .build();

            reverseToAdd.add(reverseItem);

            if((product.getStockQuantity() - Math.abs(reverseItem.getQuantity())) != 0) {
                product.setBasePrice(((oldPrice * product.getStockQuantity() - reverseItem.getPrice() * Math.abs(reverseItem.getQuantity())))  / (product.getStockQuantity() - Math.abs(reverseItem.getQuantity())));
            }
            product.setStockQuantity(product.getStockQuantity() + reverseItem.getQuantity());

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
        supplier.setTotalUnpaid(supplier.getTotalUnpaid() - supply.getTotalUnpaid());
        supplier.setTotalPaid(supplier.getTotalPaid() - supply.getTotalPaid());
        supplier.setTotalUnrefunded(supplier.getTotalUnrefunded() + supply.getTotalPaid());

        supply.setStatus(SupplyStatus.CANCELLED);
        supply.setTotalUnrefunded(supply.getTotalPaid() + supply.getTotalUnrefunded());
        supply.setTotalUnpaid(0L);
        supply.setTotalPaid(0L); 

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

        if(request.getTotalDiscount() == null) request.setTotalDiscount(0L);
        if(request.getTotalFee() == null) request.setTotalFee(0L);
        if(request.getTotalDiscount() != 0 || request.getTotalFee() != 0){
            log.debug("Updating supply totals for ID: {}, discount: {}, fee: {}", id, request.getTotalDiscount(), request.getTotalFee());
            Long totalChange = request.getTotalDiscount() - request.getTotalFee();
            Long oldTotalUnpaid = supply.getTotalUnpaid();
            Long oldTotalPaid = supply.getTotalPaid();
            Long oldTotalUnrefunded = supply.getTotalUnrefunded();

            supply.setGrandTotal(supply.getSubTotal() - totalChange);
            
            supply.setTotalUnpaid(oldTotalUnpaid - totalChange);
            Long changeTotalUnpaid = supply.getTotalUnpaid();

            if(changeTotalUnpaid < 0){
                supply.setTotalUnpaid(0L);
                supply.setTotalPaid(oldTotalPaid - Math.abs(changeTotalUnpaid));
                supply.setTotalUnrefunded(oldTotalUnrefunded + Math.abs(changeTotalUnpaid));
           }

           if(supply.getTotalUnpaid() == 0) supply.setStatus(SupplyStatus.COMPLETE);

           Supplier supplier = supply.getSupplier();
           supplier.setTotalUnpaid(supplier.getTotalUnpaid() + (changeTotalUnpaid - oldTotalUnpaid));
           supplier.setTotalPaid(supplier.getTotalPaid() + (supply.getTotalPaid() - oldTotalPaid));
           supplier.setTotalUnrefunded(supplier.getTotalUnrefunded() + (supply.getTotalUnrefunded() - oldTotalUnrefunded));
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
        long priceFromSupplier = matchItems.getLast().getPrice();
        long ttlQuantiyLeft = matchItems.stream().mapToLong(item -> item.getQuantity()).sum();

        if(ttlQuantiyLeft < request.getQuantity()) {
            log.debug("Refund request quantity {} exceeds remaining refundable stock {} for supply ID: {}", request.getQuantity(), ttlQuantiyLeft, id);
            throw new ForbiddenRequestException("refund quantity exceeds the remaining refundedable stock with quantity: " + ttlQuantiyLeft + ", enter an exact amount to proceed");
        }

        Supply supply = matchItems.getFirst().getSupply();

        if(supply.getStatus() == SupplyStatus.CANCELLED) {
            log.debug("Refund request failed - supply already cancelled: {}", id);
            throw new ForbiddenRequestException("Unable to cancel supply because Supply Status is CANCELLED");
        }

        Product product = matchItems.getFirst().getProduct();
        if(product.getStockQuantity() < request.getQuantity()) {
            log.debug("Refund request failed - insufficient stock for product ID: {}. requested {}, available {}", product.getId(), request.getQuantity(), product.getStockQuantity());
            throw new TransactionValidationException("Unable to cancel supply items, product with id " + product.getId() + " doesn't have enough stock to refund, stock left: " + product.getStockQuantity());
        }

        Long oldPrice = product.getBasePrice();
        Long oldStock = product.getStockQuantity();

        if((product.getStockQuantity() - Math.abs(request.getQuantity())) != 0) {
            product.setBasePrice(((oldPrice * product.getStockQuantity() - priceFromSupplier * Math.abs(request.getQuantity())))  / (product.getStockQuantity() - Math.abs(request.getQuantity())));
        }
 
        Long oldTotalUnpaid = supply.getTotalUnpaid();
        Long oldTotalPaid = supply.getTotalPaid();
        Long oldTotalUnrefunded = supply.getTotalUnrefunded();

        product.setStockQuantity(oldStock - request.getQuantity());
        
        SupplyItem supplyItem = SupplyItem.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .price(priceFromSupplier)
            .quantity(-request.getQuantity())
            .description("REFUNDED ")
            .product(product)
            .supply(supply)
            .build();

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

        Long totalChange = priceFromSupplier * request.getQuantity();

        supply.setTotalUnpaid(supply.getTotalUnpaid() - totalChange);
        Long changeTotalUnpaid = supply.getTotalUnpaid();
        
        if(changeTotalUnpaid < 0){
            supply.setTotalUnpaid(0L);
            supply.setTotalPaid(oldTotalPaid - Math.abs(changeTotalUnpaid));
            supply.setTotalUnrefunded(oldTotalUnrefunded + Math.abs(changeTotalUnpaid));
        }

        if(supply.getTotalUnpaid() == 0) supply.setStatus(SupplyStatus.COMPLETE);

        Supplier supplier = supply.getSupplier();
        supplier.setTotalUnpaid(supplier.getTotalUnpaid() + (changeTotalUnpaid - oldTotalUnpaid));
        supplier.setTotalPaid(supplier.getTotalPaid() + (supply.getTotalPaid() - oldTotalPaid));
        supplier.setTotalUnrefunded(supplier.getTotalUnrefunded() + (supply.getTotalUnrefunded() - oldTotalUnrefunded));

        stockCardRepository.save(stockCard);
        supplyItemRepository.save(supplyItem);
        SupplyResponse response = allSupplyMapper.createSimpleDTO(supply);
        log.debug("Supply refund response created: {}", response);
        return response;
    }
    
}
