package lumi.insert.app.service.supplypayment;

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

import lumi.insert.app.core.entity.Supplier;
import lumi.insert.app.core.entity.Supply;
import lumi.insert.app.core.entity.SupplyItem;
import lumi.insert.app.core.entity.SupplyPayment;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;
import lumi.insert.app.core.entity.nondatabase.EmployeeRole;
import lumi.insert.app.core.repository.SupplyPaymentRepository;
import lumi.insert.app.core.repository.SupplyRepository;
import lumi.insert.app.mapper.AllSupplyMapper;
import lumi.insert.app.service.implement.SupplyPaymentServiceImpl;
import lumi.insert.app.utils.generator.InvoiceGenerator;
import lumi.insert.app.utils.generator.JpaSpecGenerator;
import lumi.insert.app.mapper.AllSupplyMapperImpl;

@ExtendWith(MockitoExtension.class)
public abstract class BaseSupplyPaymentServiceTest {

    @InjectMocks
    SupplyPaymentServiceImpl supplyPaymentServiceMock;

    @Mock
    SupplyRepository supplyRepositoryMock;

    @Mock
    SupplyPaymentRepository supplyPaymentRepositoryMock;

    @Mock
    JpaSpecGenerator jpaSpecGenerator;

    @Spy
    AllSupplyMapper allSupplyMapper = new AllSupplyMapperImpl();

    @Spy
    InvoiceGenerator invoiceGenerator = new InvoiceGenerator();

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    public Supplier setupSupplier;

    public Supply setupSupply;

    public SupplyPayment setupSupplyPayment;

    public SupplyItem setupSupplyItem;

    @BeforeEach
    void setUp() {
        setupSupplyPayment = SupplyPayment.builder()
                .id(UuidCreator.getTimeOrderedEpochFast())
                .build();

        setupSupply = Supply.builder()
                .id(UuidCreator.getTimeOrderedEpochFast())
                .build();

        setupSupplyItem = SupplyItem.builder()
                .id(UuidCreator.getTimeOrderedEpochFast())
                .build();

        setupSupplier = Supplier.builder()
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
