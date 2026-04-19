package lumi.insert.app.core.repository;
 

import java.time.LocalDateTime;
import java.util.List; 
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import lumi.insert.app.core.entity.TransactionItem;
import lumi.insert.app.core.repository.projection.ProductRefund;
import lumi.insert.app.core.repository.projection.ProductSale;

/**
 * Repository for {@link TransactionItem} entity.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Repository
public interface TransactionItemRepository extends JpaRepository<TransactionItem, UUID>{
    
    List<TransactionItem> findByTransactionIdAndProductId(UUID transactionId, Long productId);

    Slice<TransactionItem> findByTransactionIdAndProductId(UUID transactionId, Long productId, Pageable pageable);

    Slice<TransactionItem> findAllByTransactionId(UUID transactionId, Pageable pageable);
 
    @Query(value = "SELECT ti.productName as productName, SUM(ti.quantity) as totalSold FROM " +
        "transaction_items ti WHERE " +
        "ti.createdAt between :startDate AND :endDate " +
        "GROUP BY ti.productName " +
        "ORDER BY SUM(ti.quantity) DESC"
    )
    List<ProductSale> getProductTopSales(@Param("startDate")LocalDateTime startDate, @Param("endDate")LocalDateTime endDate);

    @Query(value = "SELECT ti.productName as productName, SUM(ti.quantity) as totalRefunded FROM " +
        "transaction_items ti WHERE " +
        "ti.quantity < 0 AND " +
        "ti.createdAt between :startDate AND :endDate " +
        "GROUP BY ti.productName " +
        "ORDER BY SUM(ti.quantity) ASC"
    )
    List<ProductRefund> getProductTopRefund(@Param("startDate")LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
};
