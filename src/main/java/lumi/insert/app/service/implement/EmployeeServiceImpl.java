package lumi.insert.app.service.implement;
   
import java.io.IOException; 
import java.util.UUID; 

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.data.domain.Pageable; 
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
 
import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.aspect.annotation.ActivityLogger;
import lumi.insert.app.core.entity.Employee;
import lumi.insert.app.core.entity.EmployeePicture;
import lumi.insert.app.core.entity.nondatabase.ActivityAction;
import lumi.insert.app.core.entity.nondatabase.CloudinaryResponse;
import lumi.insert.app.core.repository.AuthTokenRepository;
import lumi.insert.app.core.repository.EmployeePictureRepository;
import lumi.insert.app.core.repository.EmployeeRepository;
import lumi.insert.app.dto.request.EmployeeCreateRequest;
import lumi.insert.app.dto.request.EmployeeUpdateRequest;
import lumi.insert.app.dto.request.PaginationRequest;
import lumi.insert.app.dto.response.EmployeeResponse;
import lumi.insert.app.exception.DatabaseInternalException;
import lumi.insert.app.exception.DuplicateEntityException; 
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.StorageActionException;
import lumi.insert.app.mapper.EmployeeMapper;
import lumi.insert.app.service.EmployeeService;
import lumi.insert.app.service.StorageService;

@Service
@Transactional
@Slf4j
public class EmployeeServiceImpl implements EmployeeService{

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    AuthTokenRepository authTokenRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    EmployeeMapper employeeMapper; 

    @Autowired
    StorageService storageService;

    @Autowired
    EmployeePictureRepository employeePictureRepository;

    @Override
    @ActivityLogger(
        entityName = "employees",
        action = ActivityAction.EMPLOYEE_REGISTERED,
        actionMessage = "New employee registered"
    )
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        if(employeeRepository.existsByUsername(request.getUsername())) throw new DuplicateEntityException("Employee with username " + request.getUsername() + " already exists");

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Employee employee = Employee.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .username(request.getUsername())
            .fullname(request.getFullname())
            .password(encodedPassword)
            .joinDate(request.getJoinDate())
            .build();
        
        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.createDtoResponseFromEmployee(savedEmployee);
    }

    @Override
    public EmployeeResponse getEmployee(UUID id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new NotFoundEntityException("Employee with ID " + id + " was not found"));

        return employeeMapper.createDtoResponseFromEmployee(employee);
    }

    @Override
    public Slice<EmployeeResponse> getEmployees(PaginationRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("createdAt").descending());
        Slice<Employee> employees = employeeRepository.findAll(pageable);

        return employees.map(employeeMapper::createDtoResponseFromEmployee);
    }

    @Override
    public boolean isExistsEmployeeByUsername(String username) {
        return employeeRepository.existsByUsername(username);
    }

    @Override
    @ActivityLogger(
        entityName = "employees",
        action = ActivityAction.EMPLOYEE_UPDATED,
        actionMessage = "Employee password changed"
    )
    public EmployeeResponse resetEmployeePassword(UUID id, String password) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new NotFoundEntityException("Employee with ID " + id + " was not found"));

        String encodedPassword = passwordEncoder.encode(password);

        employee.setPassword(encodedPassword);
        
        Employee savedEmployee = employeeRepository.save(employee);

        authTokenRepository.deleteByEmployeeId(employee.getId());
        return employeeMapper.createDtoResponseFromEmployee(savedEmployee);
    }

    @Override
    @ActivityLogger(
        entityName = "employees",
        action = ActivityAction.EMPLOYEE_UPDATED,
        actionMessage = "Employee updated"
    )
    public EmployeeResponse updateEmployee(UUID id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new NotFoundEntityException("Employee with ID " + id + " was not found"));

        if(request.getUsername() != null){
            if(employeeRepository.existsByUsername(request.getUsername())) throw new DuplicateEntityException("Employee with username " + request.getUsername() + " already exists");  
        }

        employeeMapper.updateEmployeeFromDto(request, employee);

        if(!employee.isActive()) authTokenRepository.deleteByEmployeeId(employee.getId());

        return employeeMapper.createDtoResponseFromEmployee(employee);
    }

    @Override
    public boolean setEmployeeProfile(UUID id, MultipartFile file){ 

        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new NotFoundEntityException("Employee with ID " + id + " was not found"));
        
        String fileName = employee.getUsername() + "-profile";

        String publicId = null;

        try { 
            CloudinaryResponse upload = storageService.uploadImageSync(file.getBytes(), fileName ,"employee");

            EmployeePicture employeePicture = EmployeePicture.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .pictureUrl(upload.getSecureUrl())
            .employee(employee)
            .publicId(upload.getPublicId())
            .build();  

            publicId = upload.getPublicId();

            employeePictureRepository.save(employeePicture);
            employee.setPictureUrl(String.valueOf(upload.getSecureUrl()));
            return true; 
        } catch (IOException e) { 
            log.error("Upload failed, messages: {}", e.getMessage());
            throw new StorageActionException("Server couldn't complete the request due to internal problem, try again or contact developer");
        } catch (Exception e) { 
            log.error("Save to database failed, attempting to delete image at storage. Messages: {}", e.getMessage()); 

            if(publicId != null) {
                if(!(storageService.deleteImage(publicId))) log.error("Failed to delete image with publicId: {}", publicId);
            }

            throw new DatabaseInternalException("Server couldn't complete the request due to internal problem, try again or contact developer");
        } 
  
    }
    
}
