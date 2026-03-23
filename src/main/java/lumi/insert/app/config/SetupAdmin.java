package lumi.insert.app.config;
  
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.github.f4b6a3.uuid.UuidCreator;

import lumi.insert.app.core.entity.Employee;
import lumi.insert.app.core.entity.nondatabase.EmployeeRole;
import lumi.insert.app.core.repository.EmployeeRepository; 

@Component
public class SetupAdmin implements CommandLineRunner{

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Value("${super.admin.password}")
    private String rawPassword;

    @Override
    public void run(String... args) throws Exception {
        if(!(employeeRepository.existsByUsername("SUPERADMIN"))){
           String encodedPassword = passwordEncoder.encode(rawPassword);

            Employee employee = Employee.builder()
                .id(UuidCreator.getTimeOrderedEpochFast())
                .username("SUPERADMIN")
                .fullname("SUPERADMIN")
                .password(encodedPassword)
                .role(EmployeeRole.OWNER)
                .joinDate(LocalDateTime.now()) 
                .build();

            employee.setCreatedBy("SETUP");
            employee.setUpdatedBy("SETUP"); 

            employeeRepository.save(employee); 
        }
    }
    
}
