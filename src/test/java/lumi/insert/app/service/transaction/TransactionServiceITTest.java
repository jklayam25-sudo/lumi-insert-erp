package lumi.insert.app.service.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue; 

import java.math.BigDecimal;
import java.util.List; 

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lumi.insert.app.TestContainerTest;
import lumi.insert.app.core.entity.Customer;
import lumi.insert.app.core.entity.Product;
import lumi.insert.app.core.entity.StockCard;
import lumi.insert.app.core.entity.Transaction;
import lumi.insert.app.core.entity.TransactionItem;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;
import lumi.insert.app.core.entity.nondatabase.EmployeeRole;
import lumi.insert.app.core.entity.nondatabase.StockMove;
import lumi.insert.app.core.entity.nondatabase.TransactionStatus;
import lumi.insert.app.core.repository.CustomerRepository;
import lumi.insert.app.core.repository.ProductRepository;
import lumi.insert.app.core.repository.StockCardRepository;
import lumi.insert.app.core.repository.TransactionItemRepository;
import lumi.insert.app.core.repository.TransactionRepository;
import lumi.insert.app.dto.response.TransactionResponse;
import lumi.insert.app.service.TransactionService;
import lumi.insert.app.service.implement.MessageProducerServiceImpl;
import lumi.insert.app.utils.generator.InvoiceGenerator;

@SpringBootTest
@Transactional
@ActiveProfiles("test") 
public class TransactionServiceITTest extends TestContainerTest {
    
    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    TransactionItemRepository transactionItemRepository;

    @Autowired
    TransactionService transactionService;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    StockCardRepository stockCardRepository;

    @Autowired
    InvoiceGenerator invoiceGenerator;

    @Autowired
    EntityManager entityManager;

    @MockitoBean
    MessageProducerServiceImpl messageProducerServiceImpl;

    Customer customer;

    @BeforeEach
    void setup(){ 
        EmployeeLogin employeeLogin = EmployeeLogin.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .username("Test Username")
            .role(EmployeeRole.CASHIER)
            .ipAddress("t.e.s.t")
            .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(employeeLogin, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        customer = customerRepository.save(Customer.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .name("TESTES")
            .contact("TESTE123")
            .shippingAddress("SHIPTEST")
            .build());
    }
    
    @Test
    @DisplayName("Should return TransactionResponse DTO when set transaction.status to Process succeed")
    public void setTransactionToProcess_validRequest_returnTransactionResponse(){
        Transaction transaction = Transaction.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .invoiceId(invoiceGenerator.generate())
            .customer(customer)
            .customerName(customer.getName())
            .build();

        Transaction savedTransaction = transactionRepository.saveAndFlush(transaction);
        Long idProduct3 = null;

        for (int i = 1; i < 5; i++) {
            Product product = Product.builder()
                .name("Product-" + i)
                .basePrice(BigDecimal.valueOf(900L * i))
                .sellPrice(BigDecimal.valueOf(1000L * i))
                .stockQuantity(BigDecimal.valueOf(10L + i))
                .build();

            Product savedProduct = productRepository.saveAndFlush(product);
            if(i == 3) idProduct3 = savedProduct.getId();

            TransactionItem transactionItem = TransactionItem.builder()
                .id(UuidCreator.getTimeOrderedEpochFast())
                .price(savedProduct.getSellPrice())
                .quantity(BigDecimal.valueOf(1L + i))
                .description(savedProduct.getName())
                .transaction(savedTransaction)
                .product(savedProduct)
                .productName(savedProduct.getName())
                .build();

            TransactionItem savedTransactionItem = transactionItemRepository.saveAndFlush(transactionItem);
            savedTransaction.getTransactionItems().add(savedTransactionItem);
        }

        Product product3 = productRepository.findById(idProduct3).orElseThrow();
        product3.setSellPrice(BigDecimal.valueOf(1000L));
        product3.setStockQuantity(BigDecimal.valueOf(0L));

        entityManager.flush();
        entityManager.clear();
 
        TransactionResponse setTransactionToProcess = transactionService.setTransactionToProcess(savedTransaction.getId());
        
        assertEquals(3L, setTransactionToProcess.totalItems()); 
        assertTrue(BigDecimal.valueOf(28000L).compareTo(setTransactionToProcess.grandTotal()) == 0);
        assertEquals(TransactionStatus.PROCESS, setTransactionToProcess.status());
        assertEquals("Item removed due to outOfStock or removed Product, Product item ID: " + product3.getId(), setTransactionToProcess.messages().getFirst());

        Slice<StockCard> stockcards = stockCardRepository.findAllByReferenceId(savedTransaction.getTransactionItems().getLast().getId());
        assertEquals(1, stockcards.getNumberOfElements());
        assertTrue(BigDecimal.valueOf(14L).compareTo(stockcards.getContent().getLast().getOldStock()) == 0);
        assertTrue(BigDecimal.valueOf(-5L).compareTo(stockcards.getContent().getLast().getQuantity()) == 0);
        assertEquals(StockMove.SALE, stockcards.getContent().getLast().getType());
        assertTrue(BigDecimal.valueOf(9L).compareTo(stockcards.getContent().getLast().getNewStock()) == 0);
        assertEquals("Product-4", stockcards.getContent().getLast().getProductName());

        product3 = productRepository.findById(idProduct3).orElseThrow();
        assertTrue(BigDecimal.valueOf(0).compareTo(product3.getStockQuantity()) == 0);
    }

    @Test
    @DisplayName("Should calculate and refresh the items, return TransactionResponse DTO when succeed")
    public void refreshTransaction_validRequest_returnTransactionResponse(){
        Transaction transaction = Transaction.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .invoiceId(invoiceGenerator.generate())
            .customer(customer)
            .customerName(customer.getName())
            .build();

        Transaction savedTransaction = transactionRepository.saveAndFlush(transaction);
        Long idProduct3 = null;

        for (int i = 1; i < 5; i++) {
            Product product = Product.builder()
                .name("Product-" + i)
                .basePrice(BigDecimal.valueOf(900L * i))
                .sellPrice(BigDecimal.valueOf(1000L * i))
                .stockQuantity(BigDecimal.valueOf(10L + i))
                .build();

            Product savedProduct = productRepository.saveAndFlush(product);
            if(i == 3) idProduct3 = savedProduct.getId();

            TransactionItem transactionItem = TransactionItem.builder()
                .id(UuidCreator.getTimeOrderedEpochFast())
                .price(savedProduct.getSellPrice())
                .quantity(BigDecimal.valueOf(1L + i))
                .description(savedProduct.getName())
                .transaction(savedTransaction)
                .product(savedProduct)
                .productName(savedProduct.getName())
                .build();

            TransactionItem savedTransactionItem = transactionItemRepository.saveAndFlush(transactionItem);
            savedTransaction.getTransactionItems().add(savedTransactionItem);
        }

        Product product3 = productRepository.findById(idProduct3).orElseThrow();
        product3.setSellPrice(BigDecimal.valueOf(1000L));
        product3.setStockQuantity(BigDecimal.valueOf(0L));                     

        entityManager.flush();
        entityManager.clear();

        TransactionResponse setTransactionToProcess = transactionService.refreshTransaction(savedTransaction.getId());
        
        assertEquals(4L, setTransactionToProcess.totalItems()); 
        assertTrue(BigDecimal.valueOf(28000L).compareTo(setTransactionToProcess.grandTotal()) == 0);
        assertEquals(TransactionStatus.PENDING, setTransactionToProcess.status());
        assertNotNull(setTransactionToProcess.messages().getFirst());

        product3 = productRepository.findById(idProduct3).orElseThrow();
        assertTrue(BigDecimal.valueOf(0).compareTo(product3.getStockQuantity()) == 0);
    }

    @Test
    @DisplayName("Should calculate and cancel the items, return TransactionResponse DTO when succeed")
    public void cancelTransaction_validRequest_returnTransactionResponse(){
        Transaction transaction = Transaction.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .invoiceId(invoiceGenerator.generate())
            .status(TransactionStatus.PROCESS)
            .totalPaid(BigDecimal.valueOf(4000L))
            .customer(customer)
            .customerName(customer.getName())
            .build();

        Transaction savedTransaction = transactionRepository.saveAndFlush(transaction);
        Long idProduct3 = null;

        for (int i = 1; i < 5; i++) {
            Product product = Product.builder()
                .name("Product-" + i)
                .basePrice(BigDecimal.valueOf(900L * i))
                .sellPrice(BigDecimal.valueOf(1000L * i))
                .stockQuantity(BigDecimal.valueOf(10L + i))
                .build();

            Product savedProduct = productRepository.saveAndFlush(product);
            if(i == 3) idProduct3 = savedProduct.getId();

            TransactionItem transactionItem = TransactionItem.builder()
                .id(UuidCreator.getTimeOrderedEpochFast())
                .price(savedProduct.getSellPrice())
                .quantity(BigDecimal.valueOf(1L + i))
                .description(savedProduct.getName())
                .transaction(savedTransaction)
                .product(savedProduct)
                .productName(savedProduct.getName())
                .build();

            TransactionItem savedTransactionItem = transactionItemRepository.saveAndFlush(transactionItem);
            savedTransaction.getTransactionItems().add(savedTransactionItem);
        }

        Product product3 = productRepository.findById(idProduct3).orElseThrow();
        product3.setSellPrice(BigDecimal.valueOf(1000L));
        product3.setStockQuantity(BigDecimal.valueOf(0L));                     

        entityManager.flush();
        entityManager.clear();

        TransactionResponse setTransactionToProcess = transactionService.cancelTransaction(savedTransaction.getId()); 
        
        assertTrue(BigDecimal.valueOf(0).compareTo(setTransactionToProcess.totalPaid()) == 0);
        assertTrue(BigDecimal.valueOf(4000).compareTo(setTransactionToProcess.totalUnrefunded()) == 0);
        assertEquals(TransactionStatus.CANCELLED, setTransactionToProcess.status()); 

        List<StockCard> stockcards = stockCardRepository.findAll(Sort.by("id").ascending());
        assertEquals(4, stockcards.size());
        assertTrue(BigDecimal.valueOf(14L).compareTo(stockcards.getLast().getOldStock()) == 0);
        assertTrue(BigDecimal.valueOf(5L).compareTo(stockcards.getLast().getQuantity()) == 0);
        assertEquals(StockMove.CUSTOMER_IN, stockcards.getLast().getType());
        assertTrue(BigDecimal.valueOf(19L).compareTo(stockcards.getLast().getNewStock()) == 0);
        assertEquals("Product-4", stockcards.getLast().getProductName());

        product3 = productRepository.findById(idProduct3).orElseThrow();
        assertTrue(BigDecimal.valueOf(4).compareTo(product3.getStockQuantity()) == 0);
    }

    @Test
    @DisplayName("Should calculate and cancel the items, return TransactionResponse DTO when succeed. CASE : CUSTOMER PAID, REFUND 1 ITEM (OUR DEBT) THEN CANCEL")
    public void cancelTransaction_validRequestCase_returnTransactionResponse(){
        Transaction transaction = Transaction.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .invoiceId(invoiceGenerator.generate())
            .status(TransactionStatus.PROCESS)
            .totalPaid(BigDecimal.valueOf(4000L))
            .totalUnrefunded(BigDecimal.valueOf(100L))
            .customer(customer)
            .customerName(customer.getName())
            .build();

        Transaction savedTransaction = transactionRepository.saveAndFlush(transaction);
        Long idProduct3 = null; 
        for (int i = 1; i < 6; i++) {
            Product product = Product.builder()
                .name("Product-" + i)
                .basePrice(BigDecimal.valueOf(900L * i))
                .sellPrice(BigDecimal.valueOf(1000L * i))
                .stockQuantity(BigDecimal.valueOf(10L + i))
                .build();

            Product savedProduct = productRepository.saveAndFlush(product);
            
            TransactionItem transactionItem = TransactionItem.builder()
                .id(UuidCreator.getTimeOrderedEpochFast())
                .price(savedProduct.getSellPrice())
                .quantity(BigDecimal.valueOf(1L + i))
                .description(savedProduct.getName())
                .transaction(savedTransaction)
                .product(savedProduct)
                .productName(savedProduct.getName())
                .build();

            TransactionItem savedTransactionItem = transactionItemRepository.saveAndFlush(transactionItem);
            if(i == 3) idProduct3 = savedProduct.getId();

            savedTransaction.getTransactionItems().add(savedTransactionItem);
        }

        Product product3 = productRepository.findById(idProduct3).orElseThrow();

        TransactionItem transactionItem = TransactionItem.builder()
                .id(UuidCreator.getTimeOrderedEpochFast())
                .price(product3.getSellPrice())
                .quantity(BigDecimal.valueOf(-2L))
                .description(product3.getName())
                .transaction(savedTransaction)
                .product(product3)
                .productName(product3.getName())
                .build();

        TransactionItem savedTransactionItem = transactionItemRepository.saveAndFlush(transactionItem);
        savedTransaction.getTransactionItems().add(savedTransactionItem);
 
        product3.setSellPrice(BigDecimal.valueOf(1000L));
        product3.setStockQuantity(BigDecimal.valueOf(0L));                     

        entityManager.flush();
        entityManager.clear();

        TransactionResponse setTransactionToProcess = transactionService.cancelTransaction(savedTransaction.getId()); 
        
        assertTrue(BigDecimal.valueOf(0).compareTo(setTransactionToProcess.totalPaid()) == 0);
        assertTrue(BigDecimal.valueOf(4100).compareTo(setTransactionToProcess.totalUnrefunded()) == 0);
        assertEquals(TransactionStatus.CANCELLED, setTransactionToProcess.status()); 

        List<StockCard> stockcards = stockCardRepository.findAll(Sort.by("id").ascending());
        assertEquals(5, stockcards.size());
        assertTrue(BigDecimal.valueOf(15L).compareTo(stockcards.getLast().getOldStock()) == 0);
        assertTrue(BigDecimal.valueOf(6L).compareTo(stockcards.getLast().getQuantity()) == 0);
        assertEquals(StockMove.CUSTOMER_IN, stockcards.getLast().getType());
        assertTrue(BigDecimal.valueOf(21L).compareTo(stockcards.getLast().getNewStock()) == 0);
        assertEquals("Product-5", stockcards.getLast().getProductName());

        product3 = productRepository.findById(idProduct3).orElseThrow();
        assertTrue(BigDecimal.valueOf(2).compareTo(product3.getStockQuantity()) == 0);

        assertEquals(11, transactionItemRepository.count());
    }
}