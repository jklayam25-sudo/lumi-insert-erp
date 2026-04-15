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

@Entity(name = "supply_payments")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Audited
public class SupplyPayment extends BaseAuditing{
  
    @Id 
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_id", nullable = false)
    @NotAudited
    private Supply supply;

    @Column(nullable = false)
    private BigDecimal totalPayment;

    @Column(nullable = false)
    private String paymentFrom;

    @Column(nullable = false)
    private String paymentTo;

    @Builder.Default
    private Boolean isForRefund = false;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @NotAudited 
    @Builder.Default
    private List<String> pictureUrl = new ArrayList<>();

    @OneToMany(mappedBy = "supplyPayment")
    @Builder.Default
    @ToString.Exclude
    @NotAudited
    private List<SupplyPaymentPicture> supplyPaymentPictures = new ArrayList<>();

    @PrePersist
    @PreUpdate
    void normalizeDecimal(){
        this.totalPayment = this.totalPayment.setScale(4, RoundingMode.HALF_UP); 
    }
}

