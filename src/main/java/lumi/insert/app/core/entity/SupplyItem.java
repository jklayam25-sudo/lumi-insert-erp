package lumi.insert.app.core.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType; 
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lumi.insert.app.core.entity.nondatabase.BaseAuditing;

/**
 * Representation class of database table {@code"supply_items"}. 
 * <p>Represent detailed supply item informations. All transaction value base on snapshot of last information before saved.<br>
 * Class can be implemented by{@code NoArgsConstructor, AllArgsConstructor and Builder}. </p>
 * 
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Entity(name = "supply_items")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder 
@Audited
public class SupplyItem extends BaseAuditing{

    @Id 
    private UUID id;

    @Builder.Default 
    private BigDecimal price = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal quantity = BigDecimal.ZERO;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false ) 
    @NotAudited
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_id", nullable = false) 
    @NotAudited
    private Supply supply;
    
    /**
     * Pre-query function used to normalize BigDecimal scale
     * <p>Rounding half up  and scaled by 4 for precise value</p>
     */
    @PrePersist
    @PreUpdate
    void normalizeDecimal(){
        this.price = this.price.setScale(4, RoundingMode.HALF_UP);
        this.quantity = this.quantity.setScale(4, RoundingMode.HALF_UP); 
    }

}
