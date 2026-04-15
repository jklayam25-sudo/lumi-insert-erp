package lumi.insert.app.service.transactionitem;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.github.f4b6a3.uuid.UuidCreator;

import lumi.insert.app.core.entity.Customer;
import lumi.insert.app.core.entity.Product;
import lumi.insert.app.core.entity.Transaction;
import lumi.insert.app.core.entity.TransactionItem;
import lumi.insert.app.core.repository.ProductRepository;
import lumi.insert.app.core.repository.StockCardRepository;
import lumi.insert.app.core.repository.TransactionItemRepository;
import lumi.insert.app.core.repository.TransactionRepository;
import lumi.insert.app.mapper.AllTransactionMapper;
import lumi.insert.app.service.implement.TransactionItemServiceImpl;
import lumi.insert.app.mapper.AllTransactionMapperImpl;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseTransactionItemServiceTest {
    
    @InjectMocks
    TransactionItemServiceImpl transactionItemServiceMock;

    @Mock
    TransactionRepository transactionRepositoryMock;

    @Mock
    ProductRepository productRepositoryMock;

    @Mock
    TransactionItemRepository transactionItemRepositoryMock;

    @Mock
    StockCardRepository stockCardRepositoryMock;

    @Spy
    AllTransactionMapper allTransactionMapper = new AllTransactionMapperImpl();

    public Transaction setupTransaction;

    public Product setupProduct;

    public TransactionItem setupTransactionItem;

    public Customer setupCustomer;

    @BeforeEach
    void setUp(){
        setupProduct = Product.builder()
        .id(999L)
        .stockQuantity(BigDecimal.valueOf(10L))
        .sellPrice(BigDecimal.valueOf(19000L))
        .build();

        setupTransaction = Transaction.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .build();

        setupTransactionItem = TransactionItem.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .build();

        setupCustomer = Customer.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .build();
    }
}
