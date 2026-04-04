package lumi.insert.app.service.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.f4b6a3.uuid.UuidCreator;

import lumi.insert.app.core.entity.Customer;
import lumi.insert.app.core.repository.CustomerPictureRepository;
import lumi.insert.app.core.repository.CustomerRepository;
import lumi.insert.app.service.StorageService;
import lumi.insert.app.service.implement.CustomerServiceImpl;
import lumi.insert.app.utils.generator.JpaSpecGenerator;
import lumi.insert.app.mapper.CustomerMapperImpl; 

@ExtendWith(MockitoExtension.class)
public abstract class BaseCustomerServiceTest {

    @InjectMocks
    CustomerServiceImpl customerServiceMock;

    @Mock
    CustomerRepository customerRepository;
    
    @Spy
    CustomerMapperImpl customerMapperImpl = new CustomerMapperImpl();

    @Spy
    JpaSpecGenerator jpaSpecGenerator = new JpaSpecGenerator();

    Customer setupCustomer;

    @Mock
    StorageService storageService;

    @Mock
    CustomerPictureRepository customerPictureRepository;
  
    @BeforeEach
    void setup(){
        setupCustomer = Customer.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .name("Test Lte.")
        .contact("Telegram - @Test") 
        .shippingAddress("St. Jose 12")
        .build();
    }
}