package lumi.insert.app.core.repository;

import java.util.Optional;
import java.util.UUID;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import lumi.insert.app.core.entity.Transaction;

/**
 * Repository for {@link Transaction} entity.
 * <p>Support {@link JpaSpecificationExecutor} query.</p>
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction>{
    
    Optional<Transaction> findByInvoiceId(String invoiceId);

    /**
     * Fetch detailed transaction (include items > product).
     * @param id
     * @return Detailed {@link Transaction}
     */
    @Query("SELECT DISTINCT t " + 
        "FROM transactions t LEFT JOIN FETCH " +
        "t.transactionItems ti LEFT JOIN FETCH " +
        "ti.product " +
        "WHERE t.id = :id " +
        "ORDER BY t.createdAt ASC")
    Optional<Transaction> findByIdDetail(@Param("id") UUID id);
 
};
