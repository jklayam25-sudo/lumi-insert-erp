package lumi.insert.app.service.stockcard;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.f4b6a3.uuid.UuidCreator;

import lumi.insert.app.core.entity.Product;
import lumi.insert.app.core.entity.TransactionItem;
import lumi.insert.app.core.repository.ProductRepository;
import lumi.insert.app.core.repository.StockCardRepository;
import lumi.insert.app.core.repository.TransactionItemRepository;
import lumi.insert.app.mapper.StockCardMapper;
import lumi.insert.app.service.implement.StockCardServiceImpl;
import lumi.insert.app.utils.generator.JpaSpecGenerator;
import lumi.insert.app.mapper.StockCardMapperImpl;

@ExtendWith(MockitoExtension.class)
public abstract class BaseStockCardServiceTest {

    @InjectMocks
    StockCardServiceImpl stockCardService;

    @Mock
    StockCardRepository stockCardRepository;

    @Mock
    TransactionItemRepository transactionItemRepository;

    @Mock
    ProductRepository productRepository;

    @Spy
    StockCardMapper stockCardMapper = new StockCardMapperImpl();

    @Spy
    JpaSpecGenerator jpaSpecGenerator = new JpaSpecGenerator();

    public Product setupProduct;

    public TransactionItem setupTransactionItem;

    @BeforeEach
    void setUp() {
        setupProduct = Product.builder()
                .id(999L)
                .name("Test product")
                .stockQuantity(BigDecimal.valueOf(10L))
                .basePrice(BigDecimal.valueOf(19000L))
                .build();

        setupTransactionItem = TransactionItem.builder()
                .id(UuidCreator.getTimeOrderedEpochFast())
                .build();
    }
}
