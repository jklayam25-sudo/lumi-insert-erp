package lumi.insert.app.service.implement;
  
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; 

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.aspect.annotation.ActivityLogger;
import lumi.insert.app.core.entity.Customer;
import lumi.insert.app.core.entity.CustomerPicture; 
import lumi.insert.app.core.entity.nondatabase.ActivityAction;
import lumi.insert.app.core.entity.nondatabase.CloudinaryResponse;
import lumi.insert.app.core.entity.nondatabase.SliceIndex;
import lumi.insert.app.core.repository.CustomerPictureRepository;
import lumi.insert.app.core.repository.CustomerRepository;
import lumi.insert.app.dto.request.CustomerCreateRequest;
import lumi.insert.app.dto.request.CustomerGetByFilter;
import lumi.insert.app.dto.request.CustomerGetNameRequest;
import lumi.insert.app.dto.request.CustomerUpdateRequest;
import lumi.insert.app.dto.response.CustomerDetailResponse;
import lumi.insert.app.dto.response.CustomerNameResponse;
import lumi.insert.app.dto.response.CustomerResponse;
import lumi.insert.app.exception.DatabaseInternalException;
import lumi.insert.app.exception.DuplicateEntityException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.StorageActionException;
import lumi.insert.app.mapper.CustomerMapper;
import lumi.insert.app.service.CustomerService;
import lumi.insert.app.service.StorageService;
import lumi.insert.app.utils.generator.JpaSpecGenerator;

@Service
@Transactional
@Slf4j
public class CustomerServiceImpl implements CustomerService{

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerMapper customerMapper;

    @Autowired
    JpaSpecGenerator jpaSpec;

    @Autowired
    StorageService storageService;

    @Autowired
    CustomerPictureRepository customerPictureRepository;

    @Override
    @ActivityLogger(
        entityName = "customers",
        action = ActivityAction.CUSTOMER_REGISTERED,
        actionMessage = "New customer registered"
    )
    public CustomerDetailResponse createCustomer(CustomerCreateRequest request) {
        if(customerRepository.existsByName(request.getName())) throw new DuplicateEntityException("Customer with name " + request.getName() + " already exists");

        Customer customer = Customer.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .name(request.getName())
            .email(request.getEmail())
            .contact(request.getContact())
            .shippingAddress(request.getShippingAddress())
            .build();

        Customer savedCustomer = customerRepository.save(customer);

        return customerMapper.createDtoDetailResponseFromEmployee(savedCustomer);
    }

    @Override
    public CustomerDetailResponse getCustomer(UUID id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundEntityException("Customer with id " + id + " is not found"));

        return customerMapper.createDtoDetailResponseFromEmployee(customer);
    }

    @Override
    public Slice<CustomerResponse> getCustomers(CustomerGetByFilter request) {
        Pageable pageable = jpaSpec.pageable(request);

        Specification<Customer> customerSpecification = jpaSpec.customerSpecification(request);

        Slice<Customer> customers = customerRepository.findAll(customerSpecification, pageable);
        return customers.map(customerMapper::createDtoResponseFromEmployee);
    }

    @Override
    public SliceIndex<CustomerNameResponse> searchCustomerNames(CustomerGetNameRequest request) {
        if(request.getLastId() == null) request.setLastId(new UUID(0, 0));
        Pageable pageable = PageRequest.of(0, request.getSize()).withSort(Sort.by("id").ascending());
         
        Slice<CustomerNameResponse> customersName = customerRepository.getByNameContainingIgnoreCaseAndIdAfter(request.getName(), request.getLastId(), pageable);;
        return new SliceIndex<>(customersName);
    }

    @Override
    @ActivityLogger(
        entityName = "customers",
        action = ActivityAction.CUSTOMER_UPDATED,
        actionMessage = "Customer updated"
    )
    public CustomerDetailResponse updateCustomer(UUID id, CustomerUpdateRequest request) {
        if(request.getName() != null && customerRepository.existsByName(request.getName())) throw new DuplicateEntityException("Customer with name " + request.getName() + " already exists");

        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundEntityException("Customer with id " + id + " is not found"));

        customerMapper.updateEntityFromDto(request, customer); 
        return customerMapper.createDtoDetailResponseFromEmployee(customer);
    }

    @Override
    public Boolean addCustomerPicture(UUID id, MultipartFile[] files) {

        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundEntityException("Customer with id " + id + " is not found"));
            
        List<String> pictureUrl = customer.getPictureUrl();
        if(pictureUrl == null) pictureUrl = new ArrayList<>();

        log.info("Customer: {}", customer);
        String fileName = customer.getName() + "-";
        List<CustomerPicture> customerPictures = new ArrayList<>();
        List<String> picturesId = new ArrayList<>();
        try {
            for (MultipartFile file : files) { 
                CloudinaryResponse upload = storageService.uploadImageSync(file.getBytes(), fileName + LocalDateTime.now() , "customer");

                CustomerPicture customerPicture = CustomerPicture.builder()
                .id(UuidCreator.getTimeOrderedEpochFast())
                .pictureUrl(upload.getSecureUrl())
                .customer(customer)
                .publicId(upload.getPublicId())
                .build(); 

                customerPictures.add(customerPicture);
                picturesId.add(upload.getPublicId());
                pictureUrl.add(upload.getSecureUrl()); 
            }
            customer.setPictureUrl(pictureUrl);
            customerPictureRepository.saveAll(customerPictures);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Upload failed, messages: " + e.getMessage());
            throw new StorageActionException("Server couldn't complete the request due to internal problem, try again or contact developer");
        } catch (Exception e) { 
            log.error("Save to database failed, attempting to delete image at storage. Messages: {}", e.getMessage()); 

            picturesId.forEach(pictureId -> {
                 if(!(storageService.deleteImage(pictureId))) log.error("Failed to delete image with publicId: {}", id);
            }); 

            throw new DatabaseInternalException("Server couldn't complete the request due to internal problem, try again or contact developer");
        }
        
        return true;

    }
    
}
