package lumi.insert.app.core.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lumi.insert.app.core.entity.nondatabase.BaseAuditing;
import lumi.insert.app.core.entity.nondatabase.TransactionStatus;
import lumi.insert.app.utils.generator.InvoiceGenerator;

/**
 * Representation class of database table {@code"transactions"}. 
 * <p>Represent detailed supply informations. All transaction value base on snapshot of last information before saved.<br>
 * Class can be implemented by{@code NoArgsConstructor, AllArgsConstructor and Builder}. </p>
 * 
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Entity(name = "transactions")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Audited
public class Transaction extends BaseAuditing{

    @Id 
    private UUID id;

    /**
     * Unique identifier Invoice ID of this transaction. 
     ** <p>Note: Strongly recommended to assign by {@link InvoiceGenerator#generate()}.</p> 
     ** <p>Important: Must not null, unique and max 55 alphs</p> 
     */
    @Column(unique = true, nullable = false, length = 55)
    private String invoiceId;

    /**
     * Total transactions related, count by increment each transactionItem saved. 
     ** <p>Note: Default value is 0</p> 
     */
    @Builder.Default 
    @NotAudited
    private Long totalItems = 0L;

    @Builder.Default
    @NotAudited
    private BigDecimal totalFee = BigDecimal.ZERO;

    @Builder.Default
    @NotAudited
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    @Builder.Default
    @NotAudited
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Builder.Default
    @NotAudited
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Builder.Default
    @NotAudited
    private BigDecimal totalUnpaid = BigDecimal.ZERO;

    @Builder.Default
    @NotAudited
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Builder.Default
    @NotAudited
    private BigDecimal totalUnrefunded = BigDecimal.ZERO;

    @Builder.Default
    @NotAudited
    private BigDecimal totalRefunded = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false) 
    @NotAudited
    private Customer customer;

    @NotAudited
    private String customerName;

    @OneToMany(mappedBy = "transaction")
    @Builder.Default
    @ToString.Exclude
    @NotAudited
    private List<TransactionItem> transactionItems = new ArrayList<>();

    @OneToMany(mappedBy = "transaction")
    @Builder.Default
    @ToString.Exclude
    @NotAudited
    private List<TransactionPayment> transactionPayments = new ArrayList<>();
    
    /**
     * Pre-query function used to normalize BigDecimal scale
     * <p>Rounding half up  and scaled by 4 for precise value</p>
     */
    @PrePersist
    @PreUpdate
    void normalizeDecimal(){
        this.totalFee = this.totalFee.setScale(4, RoundingMode.HALF_UP);
        this.totalDiscount = this.totalDiscount.setScale(4, RoundingMode.HALF_UP);
        this.subTotal = this.subTotal.setScale(4, RoundingMode.HALF_UP);
        this.grandTotal = this.grandTotal.setScale(4, RoundingMode.HALF_UP);

        this.totalUnpaid = this.totalUnpaid.setScale(4, RoundingMode.HALF_UP);
        this.totalPaid = this.totalPaid.setScale(4, RoundingMode.HALF_UP);
        this.totalUnrefunded = this.totalUnrefunded.setScale(4, RoundingMode.HALF_UP);
        this.totalRefunded = this.totalRefunded.setScale(4, RoundingMode.HALF_UP);
    }
}

