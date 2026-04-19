package lumi.insert.app.core.repository;
 
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import lumi.insert.app.core.entity.Customer;
import lumi.insert.app.dto.response.CustomerNameResponse; 

/**
 * Repository for {@link Customer} entity.
 * <p>Support {@link JpaSpecificationExecutor} query.</p>
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer>{
    
    boolean existsByName(String name);

    Slice<CustomerNameResponse> getByNameContainingIgnoreCaseAndIdAfter(String name, UUID lastId, Pageable pageable);
}
