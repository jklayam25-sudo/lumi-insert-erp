package lumi.insert.app.service.implement;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.aspect.annotation.ActivityLogger;
import lumi.insert.app.core.entity.Supplier;
import lumi.insert.app.core.entity.nondatabase.ActivityAction;
import lumi.insert.app.core.entity.nondatabase.SliceIndex;
import lumi.insert.app.core.repository.SupplierRepository;
import lumi.insert.app.dto.request.SupplierCreateRequest;
import lumi.insert.app.dto.request.SupplierGetByFilter;
import lumi.insert.app.dto.request.SupplierGetNameRequest;
import lumi.insert.app.dto.request.SupplierUpdateRequest;
import lumi.insert.app.dto.response.SupplierDetailResponse;
import lumi.insert.app.dto.response.SupplierNameResponse;
import lumi.insert.app.exception.DuplicateEntityException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.mapper.SupplierMapper;
import lumi.insert.app.service.SupplierService;
import lumi.insert.app.utils.generator.JpaSpecGenerator;

@Service
@Transactional
@Slf4j
public class SupplierServiceImpl implements SupplierService{

    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    SupplierMapper supplierMapper;

    @Autowired
    JpaSpecGenerator jpaSpecGenerator;

    @Override
    @ActivityLogger(
        entityName = "suppliers",
        action = ActivityAction.SUPPLIER_REGISTERED,
        actionMessage = "New supplier registered"
    )
    public SupplierDetailResponse createSupplier(SupplierCreateRequest request) {
        log.info("Creating supplier with name: {}", request.getName());
        if(supplierRepository.existsByName(request.getName())) {
            log.debug("Supplier creation failed - duplicate name: {}", request.getName());
            throw new DuplicateEntityException("Supplier with name " + request.getName() + " already exists");
        }

        Supplier supplier = Supplier.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .name(request.getName())
            .email(request.getEmail())
            .contact(request.getContact()) 
            .build();

        Supplier savedSupplier = supplierRepository.save(supplier);
        log.debug("Supplier saved to database: {}", savedSupplier);
        SupplierDetailResponse response = supplierMapper.createDtoDetailResponseFromSupplier(savedSupplier);
        log.debug("Supplier response created: {}", response);
        return response;
    }

    @Override
    public SupplierDetailResponse getSupplier(UUID id) {
        log.debug("Getting supplier by ID: {}", id);
        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> {
                log.debug("Supplier not found with ID: {}", id);
                return new NotFoundEntityException("Supplier with id " + id + " is not found");
            });

        SupplierDetailResponse response = supplierMapper.createDtoDetailResponseFromSupplier(supplier);
        log.debug("Supplier response created: {}", response);
        return response;
    }

    @Override
    public Slice<SupplierDetailResponse> getSuppliers(SupplierGetByFilter request) {
        log.debug("Getting suppliers with filter: {}", request);
        Pageable pageable = jpaSpecGenerator.pageable(request);

        Specification<Supplier> supplierSpecification = jpaSpecGenerator.supplierSpecification(request);

        Slice<Supplier> suppliers = supplierRepository.findAll(supplierSpecification, pageable);
        log.debug("Found {} suppliers", suppliers.getNumberOfElements());
        return suppliers.map(supplierMapper::createDtoDetailResponseFromSupplier);
    }

    @Override
    public SliceIndex<SupplierNameResponse> searchSupplierNames(SupplierGetNameRequest request) {
        log.debug("Searching supplier names with query: {}, size: {}", request.getName(), request.getSize());
        if(request.getLastId() == null) request.setLastId(new UUID(0, 0));
        Pageable pageable = PageRequest.of(0, request.getSize()).withSort(Sort.by("id").ascending());
        
        Slice<SupplierNameResponse> suppliersName = supplierRepository.getByNameContainingIgnoreCaseAndIdAfter(request.getName(), request.getLastId(), pageable);
        log.debug("Found {} supplier names", suppliersName.getNumberOfElements());
        return new SliceIndex<SupplierNameResponse>(suppliersName);
    }

    @Override
    @ActivityLogger(
        entityName = "suppliers",
        action = ActivityAction.SUPPLIER_UPDATED,
        actionMessage = "Supplier updated"
    )
    public SupplierDetailResponse updateSupplier(UUID id, SupplierUpdateRequest request) {
        log.info("Updating supplier with ID: {}", id);
        if(request.getName() != null && supplierRepository.existsByName(request.getName())) {
            log.debug("Supplier update failed - duplicate name: {}", request.getName());
            throw new DuplicateEntityException("Supplier with name " + request.getName() + " already exists");
        }

        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> {
                log.debug("Supplier not found for update with ID: {}", id);
                return new NotFoundEntityException("Supplier with id " + id + " is not found");
            });

        supplierMapper.updateEntityFromDto(request, supplier);
        SupplierDetailResponse response = supplierMapper.createDtoDetailResponseFromSupplier(supplier);
        log.debug("Supplier response created: {}", response);
        return response;
    }
    
}
