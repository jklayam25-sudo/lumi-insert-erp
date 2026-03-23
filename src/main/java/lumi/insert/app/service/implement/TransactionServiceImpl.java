package lumi.insert.app.service.implement;

import java.util.ArrayList; 
import java.util.HashSet;
import java.util.List;
import java.util.Map; 
import java.util.Set;
import java.util.UUID;
import java.util.function.Function; 

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice; 
import org.springframework.data.jpa.domain.Specification; 
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.aspect.annotation.ActivityLogger;
import lumi.insert.app.core.entity.Customer;
import lumi.insert.app.core.entity.Product;
import lumi.insert.app.core.entity.StockCard;
import lumi.insert.app.core.entity.Transaction;
import lumi.insert.app.core.entity.TransactionItem;
import lumi.insert.app.core.entity.nondatabase.ActivityAction;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;
import lumi.insert.app.core.entity.nondatabase.StockMove;
import lumi.insert.app.core.entity.nondatabase.TransactionInvoiceMail;
import lumi.insert.app.core.entity.nondatabase.TransactionStatus;
import lumi.insert.app.core.repository.CustomerRepository;
import lumi.insert.app.core.repository.ProductRepository;
import lumi.insert.app.core.repository.StockCardRepository;
import lumi.insert.app.core.repository.TransactionItemRepository;
import lumi.insert.app.core.repository.TransactionRepository;
import lumi.insert.app.core.repository.projection.ProductRefreshProjection;
import lumi.insert.app.dto.request.TransactionCreateRequest;
import lumi.insert.app.dto.request.TransactionGetByFilter;
import lumi.insert.app.dto.response.TransactionDetailResponse;
import lumi.insert.app.dto.response.TransactionResponse;
import lumi.insert.app.exception.BoilerplateRequestException;
import lumi.insert.app.exception.ForbiddenRequestException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.TransactionValidationException;
import lumi.insert.app.mapper.AllTransactionMapper;
import lumi.insert.app.service.MessageProducerService;
import lumi.insert.app.service.TransactionService;
import lumi.insert.app.utils.generator.InvoiceGenerator;
import lumi.insert.app.utils.generator.JpaSpecGenerator;

@Service
@Slf4j
@Transactional
public class TransactionServiceImpl implements TransactionService{

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    ProductRepository productRepository;
    
    @Autowired
    TransactionItemRepository transactionItemRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    StockCardRepository stockCardRepository;

    @Autowired
    InvoiceGenerator invoiceGenerator;

    @Autowired
    AllTransactionMapper allTransactionMapper;

    @Autowired
    JpaSpecGenerator jpaSpecGenerator;

    @Autowired
    MessageProducerService messageProducerService;

    @Override
    @ActivityLogger(
        entityName = "transactions",
        action = ActivityAction.TRANSACTION_CART_CREATED,
        actionMessage = "New transaction cart created"
    )
    public TransactionResponse createTransaction(TransactionCreateRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new NotFoundEntityException("Customer with ID " + request.getCustomerId() + " is not found"));

        if(customer.getIsActive() == false) throw new TransactionValidationException("Customer with ID " + request.getCustomerId() + " is not active");

        Transaction transaction = Transaction.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .customerName(customer.getName())
            .invoiceId(invoiceGenerator.generate())
            .customer(customer)
            .build();

        log.info("{}", transaction);

        Transaction savedTransaction = transactionRepository.save(transaction);
        return allTransactionMapper.createTransactionResponseDto(savedTransaction);
    }

    @Override
    public Slice<TransactionResponse> searchTransactionsByRequests(TransactionGetByFilter request) {
        Pageable pageable = jpaSpecGenerator.pageable(request);

        Specification<Transaction> specification = jpaSpecGenerator.transactionSpecification(request);

        Slice<Transaction> transactions = transactionRepository.findAll(specification, pageable);

        Slice<TransactionResponse> result = transactions.map(allTransactionMapper::createTransactionResponseDto);

        return result;
    }

    @Override
    @ActivityLogger(
        entityName = "transactions",
        action = ActivityAction.TRANSACTION_ORDER_PLACED,
        actionMessage = "Transaction order placed"
    )
    public TransactionResponse setTransactionToProcess(UUID id) {
        List<String> messages = new ArrayList<>();

        Transaction searchedTransaction = transactionRepository.findById(id)
            .orElseThrow(() -> new NotFoundEntityException("Transaction with ID " + id + " was not found"));
        
        if(searchedTransaction.getStatus() == null || searchedTransaction.getStatus() != TransactionStatus.PENDING) throw new ForbiddenRequestException("Unable to process transaction because Transaction Status is not PENDING(CART)");
        List<TransactionItem> transactionItems = searchedTransaction.getTransactionItems(); 

        List<Long> listProductIdFromTrxItems = transactionItems.stream().map(item -> item.getProduct().getId()).distinct().toList();
        List<Product> listProductFromTrxItemsUpdated = productRepository.findAllById(listProductIdFromTrxItems);

        Map<Long, Product> productMap = listProductFromTrxItemsUpdated.stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        Set<UUID> listOfOutStockAndRemovedProduct = new HashSet<>();

        List<StockCard> stockCards = new ArrayList<>();

        transactionItems.forEach(item -> {
            Product updatedProduct = productMap.get(item.getProduct().getId()); 

            Long oldStock = updatedProduct.getStockQuantity();

            if(updatedProduct == null || updatedProduct.getStockQuantity() == 0) {
                listOfOutStockAndRemovedProduct.add(item.getId());
                messages.add("Item removed due to outOfStock or removed Product, Product item ID: " + item.getProduct().getId());
                return;
            }

            item.setPrice(updatedProduct.getSellPrice());

            if(updatedProduct.getStockQuantity() < item.getQuantity()){
                messages.add(updatedProduct.getName() + " stock lesser than " + item.getQuantity() + ", quantity decreased to " + updatedProduct.getStockQuantity());
                item.setQuantity(updatedProduct.getStockQuantity());
            }
            updatedProduct.setStockQuantity(updatedProduct.getStockQuantity()-item.getQuantity());

            StockCard stockCard = StockCard.builder()
                .id(UuidCreator.getTimeOrderedEpochFast())
                .referenceId(item.getId())
                .product(updatedProduct)
                .productName(updatedProduct.getName())
                .quantity(-item.getQuantity())
                .oldStock(oldStock)
                .newStock(updatedProduct.getStockQuantity())
                .type(StockMove.SALE)
                .oldPrice(updatedProduct.getBasePrice())
                .newPrice(updatedProduct.getBasePrice())
                .description("Product sale(OUT)")
                .build();

            stockCards.add(stockCard);
        }); 

        if (listOfOutStockAndRemovedProduct.size() != 0) {
            transactionItemRepository.deleteAllByIdInBatch(listOfOutStockAndRemovedProduct);
            transactionItems.removeIf(item -> listOfOutStockAndRemovedProduct.contains(item.getId()));
        }

        stockCardRepository.saveAll(stockCards);
        searchedTransaction.setTotalItems(Long.valueOf(transactionItems.size()));
        searchedTransaction.setSubTotal(transactionItems.stream().mapToLong(item -> item.getPrice() * item.getQuantity()).sum());
        searchedTransaction.setGrandTotal(searchedTransaction.getSubTotal() - searchedTransaction.getTotalDiscount() + searchedTransaction.getTotalFee());
        searchedTransaction.setStatus(TransactionStatus.PROCESS);

        Customer customer = searchedTransaction.getCustomer();
        customer.setTotalUnpaid(customer.getTotalUnpaid() + searchedTransaction.getGrandTotal());
         
        messageProducerService.sendTransactionInvoiceEmail(new TransactionInvoiceMail(id, customer.getEmail(), ((EmployeeLogin) SecurityContextHolder.getContext().getAuthentication().getPrincipal())));
        return allTransactionMapper.createTransactionResponseDto(searchedTransaction, messages);
    }

    @Override
    @ActivityLogger(
        entityName = "transactions",
        action = ActivityAction.TRANSACTION_ORDER_COMPLETED,
        actionMessage = "Transaction order completed"
    )
    public TransactionResponse setTransactionToComplete(UUID id) {
        Transaction searchedTransaction = transactionRepository.findById(id)
            .orElseThrow(() -> new NotFoundEntityException("Transaction with ID " + id + " was not found"));
            
        if(searchedTransaction.getStatus() == TransactionStatus.COMPLETE) throw new BoilerplateRequestException("Transaction with ID " + id + " already process");
        if(searchedTransaction.getStatus() != TransactionStatus.PROCESS) throw new ForbiddenRequestException("Transaction with ID " + id + " is " + searchedTransaction.getStatus() + " and can't be set to COMPLETE");

        searchedTransaction.setStatus(TransactionStatus.COMPLETE);

        return allTransactionMapper.createTransactionResponseDto(searchedTransaction);
    }

    @Override
    @ActivityLogger(
        entityName = "transactions",
        action = ActivityAction.TRANSACTION_ORDER_CANCELLED,
        actionMessage = "Transaction order cancelled"
    )
    public TransactionResponse cancelTransaction(UUID id) {
        Transaction searchedTransaction = transactionRepository.findById(id)
            .orElseThrow(() -> new NotFoundEntityException("Transaction with ID " + id + " was not found"));

        if (searchedTransaction.getStatus() != TransactionStatus.PROCESS && searchedTransaction.getStatus() != TransactionStatus.COMPLETE ) throw new ForbiddenRequestException("Unable to cancel transaction because Transaction Status is not PROCESS OR COMPLETE");

        List<TransactionItem> transactionItems = searchedTransaction.getTransactionItems();
        List<Long> listProductIdFromTrxItems = transactionItems.stream().map(item -> item.getProduct().getId()).distinct().toList();
        List<Product> listProductFromTrxItemsUpdated = productRepository.findAllById(listProductIdFromTrxItems);

        Map<Long, Product> productMap = listProductFromTrxItemsUpdated.stream().collect(Collectors.toMap(Product::getId, Function.identity()));
 
        Map<Long, List<TransactionItem>> listRefunded = transactionItems.stream().filter(item -> item.getQuantity() < 0).collect(Collectors.groupingBy(
            item -> item.getProduct().getId()));

        List<StockCard> stockCards = new ArrayList<>();

        List<TransactionItem> toRefundItems = new ArrayList<>();

        for(TransactionItem item: transactionItems){
            if(item.getQuantity() < 0) { 
                continue;
            }; 

            List<TransactionItem> transactionItem = listRefunded.get(item.getProduct().getId());

            Product product = productMap.get(item.getProduct().getId());
            if(product == null) continue;

            Long cancelledQuantity;

            UUID refId;
            if(transactionItem != null){
                Long totalRefund = transactionItem.stream().mapToLong(reduce -> reduce.getQuantity()).sum();

                TransactionItem reverseItem = TransactionItem.builder()
                    .id(UuidCreator.getTimeOrderedEpochFast())
                    .price(item.getPrice())
                    .productName(product.getName())
                    .quantity(-(item.getQuantity() + totalRefund))
                    .description("CANCELLED: " + product.getName())
                    .product(product)
                    .transaction(searchedTransaction)
                    .build();

                cancelledQuantity = item.getQuantity() + totalRefund;
                refId = reverseItem.getId();
                toRefundItems.add(reverseItem);
            } else {
                TransactionItem reverseItem = TransactionItem.builder()
                    .id(UuidCreator.getTimeOrderedEpochFast())
                    .price(item.getPrice())
                    .productName(product.getName())
                    .quantity(-(item.getQuantity()))
                    .description("CANCELLED: " + product.getName())
                    .product(product)
                    .transaction(searchedTransaction)
                    .build();

                cancelledQuantity = item.getQuantity();
                refId = reverseItem.getId();
                toRefundItems.add(reverseItem);
            }

            Long oldStock = product.getStockQuantity();

            product.setStockQuantity(product.getStockQuantity() + cancelledQuantity);

            StockCard stockCard = StockCard.builder()
                .id(UuidCreator.getTimeOrderedEpochFast())
                .referenceId(refId)
                .product(product)
                .productName(product.getName())
                .quantity(cancelledQuantity)
                .oldStock(oldStock)
                .newStock(product.getStockQuantity())
                .type(StockMove.CUSTOMER_IN)
                .oldPrice(product.getBasePrice())
                .newPrice(product.getBasePrice())
                .description("Transaction Cancelled, Product refunded. Status: CUSTOMER_IN(IN)")
                .build();
                
            log.info("{}", stockCard);
            stockCards.add(stockCard);
        }

        transactionItemRepository.saveAll(toRefundItems);
        stockCardRepository.saveAll(stockCards);

        Long totalPaid = searchedTransaction.getTotalPaid();
        Long totalUnpaid = searchedTransaction.getTotalUnpaid();

        searchedTransaction.setTotalUnrefunded(searchedTransaction.getTotalPaid() + searchedTransaction.getTotalUnrefunded());
        searchedTransaction.setTotalUnpaid(0L);
        searchedTransaction.setTotalPaid(0L); 
        searchedTransaction.setStatus(TransactionStatus.CANCELLED);

        Customer customer = searchedTransaction.getCustomer();
        customer.setTotalUnpaid(customer.getTotalUnpaid() - totalUnpaid);
        customer.setTotalPaid(customer.getTotalPaid() - totalPaid);
        customer.setTotalUnrefunded(customer.getTotalUnrefunded() + totalPaid);

        TransactionResponse transactionResponseDto = allTransactionMapper.createTransactionResponseDto(searchedTransaction);
        return transactionResponseDto;
    }

    @Override
    public TransactionResponse getTransaction(UUID id) {
        Transaction searchedTransaction = transactionRepository.findById(id)
            .orElseThrow(() -> new NotFoundEntityException("Transaction with ID " + id + " was not found"));

        return allTransactionMapper.createTransactionResponseDto(searchedTransaction);
    }

    @Override
    public TransactionDetailResponse getTransactionDetail(UUID id) {
        Transaction searchedTransaction = transactionRepository.findByIdDetail(id)
            .orElseThrow(() -> new NotFoundEntityException("Transaction with ID " + id + " was not found"));

        return allTransactionMapper.createTransactionDetailResponseDto(searchedTransaction);
    }

    @Override
    public TransactionResponse refreshTransaction(UUID id) {
        List<String> messages= new ArrayList<>();

        Transaction searchedTransaction = transactionRepository.findById(id)
            .orElseThrow(() -> new NotFoundEntityException("Transaction with ID " + id + " was not found"));

        if (searchedTransaction.getStatus() != TransactionStatus.PENDING) throw new ForbiddenRequestException("Unable to refresh transaction because Transaction Status is not PENDING(CART)");

        List<TransactionItem> transactionItems = searchedTransaction.getTransactionItems();
        List<Long> listProductIdFromTrxItems = transactionItems.stream().map(item -> item.getProduct().getId()).distinct().toList();
        List<ProductRefreshProjection> listProductFromTrxItemsUpdated = productRepository.searchIdUpdatedAtMoreThan(listProductIdFromTrxItems, searchedTransaction.getCreatedAt());

        Map<Long, ProductRefreshProjection>productMap = listProductFromTrxItemsUpdated.stream().collect(Collectors.toMap(ProductRefreshProjection::id, Function.identity()));

        transactionItems.forEach(item -> {
            ProductRefreshProjection updatedProduct = productMap.get(item.getProduct().getId());
            if(updatedProduct == null) return;

            item.setPrice(updatedProduct.sellPrice());

            if(updatedProduct.stockQuantity() < item.getQuantity()){
                messages.add("Product stock lesser than " + item.getQuantity() + ", transaction quantity decreased to " + updatedProduct.stockQuantity());
                item.setQuantity(updatedProduct.stockQuantity());
            }
        });
        
        searchedTransaction.setTotalItems(Long.valueOf(transactionItems.size()));
        searchedTransaction.setSubTotal(transactionItems.stream().mapToLong(item -> item.getPrice() * item.getQuantity()).sum());
        searchedTransaction.setGrandTotal(searchedTransaction.getSubTotal() - searchedTransaction.getTotalDiscount() + searchedTransaction.getTotalFee());

        TransactionResponse transactionResponseDto = allTransactionMapper.createTransactionResponseDto(searchedTransaction, messages);
        return transactionResponseDto;

    }
    
}
