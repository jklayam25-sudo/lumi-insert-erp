package lumi.insert.app.core.entity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Representation class of database table {@code"transaction_payments"}. 
 * <p>Represent detailed transaction payment informations.<br>
 * Class can be implemented by{@code NoArgsConstructor, AllArgsConstructor and Builder}. </p>
 * 
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Entity(name = "transaction_payments")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Audited
public class TransactionPayment extends BaseAuditing{
  
    @Id 
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    @NotAudited
    private Transaction transaction;

    @Column(nullable = false)
    private BigDecimal totalPayment;

    /**
     * Payment bank account/information of customer.  
     */
    @Column(nullable = false)
    private String paymentFrom;

    /**
     * Payment bank account/information of our company.  
     */
    @Column(nullable = false)
    private String paymentTo;

     /**
     * When set to true mean this payment is meant to refund.
     * <p>Example: Case when company has debt(unrefund) and want to pay by cash or item (return).</p>  
     */
    @Builder.Default
    private Boolean isForRefund = false;

    /**
     * Payment picture url.  
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @NotAudited 
    @Builder.Default
    private List<String> pictureUrl = new ArrayList<>();

    /**
     * Payment picture related.  
     */
    @OneToMany(mappedBy = "transactionPayment")
    @Builder.Default
    @ToString.Exclude
    @NotAudited
    private List<TransactionPaymentPicture> transactionPaymentPictures = new ArrayList<>();

    /**
     * Pre-query function used to normalize BigDecimal scale
     * <p>Rounding half up  and scaled by 4 for precise value</p>
     */
    @PrePersist
    @PreUpdate
    void normalizeDecimal(){
        this.totalPayment = this.totalPayment.setScale(4, RoundingMode.HALF_UP); 
    }
}

