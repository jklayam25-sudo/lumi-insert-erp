package lumi.insert.app.service.transactionpayment;
 

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.github.f4b6a3.uuid.UuidCreator;

import lumi.insert.app.core.entity.Customer;
import lumi.insert.app.core.entity.Transaction;
import lumi.insert.app.core.entity.TransactionItem;
import lumi.insert.app.core.entity.TransactionPayment;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;
import lumi.insert.app.core.entity.nondatabase.EmployeeRole;
import lumi.insert.app.core.repository.TransactionPaymentRepository;
import lumi.insert.app.core.repository.TransactionRepository;
import lumi.insert.app.mapper.AllTransactionMapper;
import lumi.insert.app.service.implement.TransactionPaymentServiceImpl; 
import lumi.insert.app.utils.generator.InvoiceGenerator;
import lumi.insert.app.utils.generator.JpaSpecGenerator;
import lumi.insert.app.mapper.AllTransactionMapperImpl;
 
@ExtendWith(MockitoExtension.class)
public abstract class BaseTransactionPaymentServiceTest {
    
    @InjectMocks
    TransactionPaymentServiceImpl transactionPaymentServiceMock;

    @Mock
    TransactionRepository transactionRepositoryMock;

    @Mock
    TransactionPaymentRepository transactionPaymentRepositoryMock;

    @Mock
    JpaSpecGenerator jpaSpecGenerator;

    @Spy
    AllTransactionMapper allTransactionMapper = new AllTransactionMapperImpl();

    @Spy
    InvoiceGenerator invoiceGenerator = new InvoiceGenerator();

    @Mock
    ApplicationEventPublisher eventPublisher;

    public Transaction setupTransaction;

    public TransactionPayment setupTransactionPayment;

    public TransactionItem setupTransactionItem;

    public Customer setupCustomer;

    @BeforeEach
    void setUp(){
        setupTransactionPayment = TransactionPayment.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
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

        EmployeeLogin employeeLogin = EmployeeLogin.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .username("Test Username")
        .role(EmployeeRole.CASHIER)
        .ipAddress("t.e.s.t")
        .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(employeeLogin, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
