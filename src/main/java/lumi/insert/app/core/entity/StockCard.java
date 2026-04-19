package lumi.insert.app.core.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
 

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import lumi.insert.app.core.entity.nondatabase.StockMove;

/**
 * Representation class of database table {@code"stock_cards"}. 
 * <p>Store product movement information such as snapshot, quantity, etc.<br>
 * Class can be implemented by{@code NoArgsConstructor, AllArgsConstructor and Builder}. </p>
 * 
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Entity(name = "stock_cards")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class StockCard extends BaseAuditing{
    
    @Id 
    UUID id;

    @Column(nullable = false)
    UUID referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) 
    private Product product;

    @Column(nullable = false) 
    private String productName;

    @Column(nullable = false)    
    private BigDecimal quantity;

    @Column(nullable = false)    
    private BigDecimal oldStock;

    @Column(nullable = false)    
    private BigDecimal newStock;

    @Column(nullable = false)    
    private BigDecimal oldPrice;

    @Column(nullable = false)    
    private BigDecimal newPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)   
    private StockMove type;

    private String description;

    /**
     * Pre-query function used to normalize BigDecimal scale
     * <p>Rounding half up  and scaled by 4 for precise value</p>
     */
    @PrePersist
    @PreUpdate
    void normalizeDecimal(){
        this.quantity = this.quantity.setScale(4, RoundingMode.HALF_UP);
        this.oldStock = this.oldStock.setScale(4, RoundingMode.HALF_UP);
        this.newStock = this.newStock.setScale(4, RoundingMode.HALF_UP);
        this.oldPrice = this.oldPrice.setScale(4, RoundingMode.HALF_UP);
        this.newPrice = this.newPrice.setScale(4, RoundingMode.HALF_UP);
    }
}
