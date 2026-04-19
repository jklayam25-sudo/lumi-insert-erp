package lumi.insert.app.core.repository;
 
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import lumi.insert.app.core.entity.TransactionPayment;

/**
 * Repository for {@link TransactionPayment} entity.
 * <p>Support {@link JpaSpecificationExecutor} query.</p>
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Repository
public interface TransactionPaymentRepository extends  JpaRepository<TransactionPayment, UUID>, JpaSpecificationExecutor<TransactionPayment>{
    
    Slice<TransactionPayment> findAllByTransactionId(UUID transactionId, Pageable pageable);

}
