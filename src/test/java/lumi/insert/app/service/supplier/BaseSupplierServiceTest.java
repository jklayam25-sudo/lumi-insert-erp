package lumi.insert.app.service.supplier;
  

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.github.f4b6a3.uuid.UuidCreator;

import lumi.insert.app.core.entity.Supplier;
import lumi.insert.app.core.repository.SupplierRepository;
import lumi.insert.app.service.implement.SupplierServiceImpl;
import lumi.insert.app.utils.generator.JpaSpecGenerator;
import lumi.insert.app.mapper.SupplierMapperImpl; 

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseSupplierServiceTest {

    @InjectMocks
    SupplierServiceImpl supplierServiceMock;

    @Mock
    SupplierRepository supplierRepository;
    
    @Spy
    SupplierMapperImpl supplierMapperImpl = new SupplierMapperImpl();

    @Spy
    JpaSpecGenerator jpaSpecGenerator = new JpaSpecGenerator();

    Supplier setupSupplier;
  
    @BeforeEach
    void setup(){
        setupSupplier = Supplier.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .name("Test Lte.")
        .contact("Telegram - @Test")  
        .build();
    }
}