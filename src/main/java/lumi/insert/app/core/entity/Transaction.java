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

    @Column(unique = true, nullable = false, length = 55)
    private String invoiceId;

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

