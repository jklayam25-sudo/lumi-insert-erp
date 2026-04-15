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

import jakarta.persistence.Id;
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

@Entity(name = "suppliers")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Audited
public class Supplier extends BaseAuditing{
    
    @Id 
    private UUID id;

    @Column(unique = true, nullable = false )
    private String name;
 
    private String email;

    @Column(nullable = false )
    private String contact;  

    @Builder.Default 
    private Boolean isActive = true;

    @Builder.Default 
    @NotAudited
    private Long totalTransaction = 0L;

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
    private BigDecimal totalRefunded =  BigDecimal.ZERO;

    @OneToMany(mappedBy = "supplier")
    @Builder.Default
    @ToString.Exclude
    @NotAudited
    private List<Supply> supplies = new ArrayList<>();

    public void addTransaction(){
        this.totalTransaction ++;
    }

    @PrePersist
    @PreUpdate
    void normalizeDecimal(){
        this.totalUnpaid = this.totalUnpaid.setScale(4, RoundingMode.HALF_UP);
        this.totalPaid = this.totalPaid.setScale(4, RoundingMode.HALF_UP);
        this.totalUnrefunded = this.totalUnrefunded.setScale(4, RoundingMode.HALF_UP);
        this.totalRefunded = this.totalRefunded.setScale(4, RoundingMode.HALF_UP);
    }
}
