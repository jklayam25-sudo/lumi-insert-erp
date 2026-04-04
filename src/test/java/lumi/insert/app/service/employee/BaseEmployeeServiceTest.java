package lumi.insert.app.service.employee;

import java.time.LocalDateTime; 

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.github.f4b6a3.uuid.UuidCreator;

import lumi.insert.app.core.entity.Employee;
import lumi.insert.app.core.repository.AuthTokenRepository;
import lumi.insert.app.core.repository.EmployeePictureRepository;
import lumi.insert.app.core.repository.EmployeeRepository;
import lumi.insert.app.service.StorageService;
import lumi.insert.app.service.implement.EmployeeServiceImpl;
import lumi.insert.app.mapper.EmployeeMapperImpl;

@ExtendWith(MockitoExtension.class)
public abstract class BaseEmployeeServiceTest {
    
    @InjectMocks
    EmployeeServiceImpl employeeServiceMock;

    @Mock
    EmployeeRepository employeeRepositoryMock;

    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @Mock
    AuthTokenRepository authTokenRepositoryMock;

    @Mock
    StorageService storageService;

    @Mock
    EmployeePictureRepository employeePictureRepository;

    @Spy
    EmployeeMapperImpl employeeMapperImpl = new EmployeeMapperImpl();

    Employee setupEmployee; 

    @BeforeEach
    void setup(){
        setupEmployee = Employee.builder()
        .id(UuidCreator.getTimeOrderedEpochFast())
        .username("testUsername")
        .fullname("testFullname")
        .password("SECRET")
        .joinDate(LocalDateTime.of(2020, 10, 10, 10, 10))
        .lastIp("xxx.xxx.xxx.xxx")
        .isActive(false)
        .build();
    }

}
