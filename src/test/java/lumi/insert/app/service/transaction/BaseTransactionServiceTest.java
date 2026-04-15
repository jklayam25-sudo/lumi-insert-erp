package lumi.insert.app.service.transaction;
 


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.github.f4b6a3.uuid.UuidCreator;

import lumi.insert.app.TestContainerTest;
import lumi.insert.app.core.entity.Customer;
import lumi.insert.app.core.entity.Product;
import lumi.insert.app.core.entity.Transaction;
import lumi.insert.app.core.entity.TransactionItem;
import lumi.insert.app.core.entity.TransactionPayment;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;
import lumi.insert.app.core.entity.nondatabase.EmployeeRole;
import lumi.insert.app.core.repository.CustomerRepository;
import lumi.insert.app.core.repository.ProductRepository;
import lumi.insert.app.core.repository.StockCardRepository;
import lumi.insert.app.core.repository.TransactionItemRepository;
import lumi.insert.app.core.repository.TransactionRepository;
import lumi.insert.app.mapper.AllTransactionMapper;
import lumi.insert.app.service.implement.TransactionServiceImpl;
import lumi.insert.app.utils.generator.InvoiceGenerator;
import lumi.insert.app.utils.generator.JpaSpecGenerator;
import lumi.insert.app.mapper.AllTransactionMapperImpl;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@SpringBootTest 
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseTransactionServiceTest extends TestContainerTest {
    
    @InjectMocks
    TransactionServiceImpl transactionServiceMock;

    @Mock
    TransactionRepository transactionRepositoryMock;

    @Mock
    TransactionItemRepository transactionItemRepositoryMock;

    @Mock
    StockCardRepository stockCardRepositoryMock;

    @Mock
    ProductRepository productRepositoryMock;

    @Mock
    CustomerRepository customerRepositoryMock;

    @Mock
    JpaSpecGenerator jpaSpecGenerator;

    @Spy
    AllTransactionMapper allTransactionMapper = new AllTransactionMapperImpl();

    @Spy
    InvoiceGenerator invoiceGenerator = new InvoiceGenerator();

    
    public Transaction setupTransaction;

    public TransactionPayment setupTransactionPayment;

    public TransactionItem setupTransactionItem;

    public Product setupProduct;

    public Customer setupCustomer;

    @BeforeEach
    void setUp(){
        EmployeeLogin employeeLogin = EmployeeLogin.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .username("Test Username")
        .role(EmployeeRole.CASHIER)
        .ipAddress("t.e.s.t")
        .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(employeeLogin, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        setupTransactionPayment = TransactionPayment.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .build();

        setupTransaction = Transaction.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .build();

        setupTransactionItem = TransactionItem.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .build();

        setupProduct = Product.builder()
        .id(1L)
        .build();

        setupCustomer = Customer.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .build();
    }
}
