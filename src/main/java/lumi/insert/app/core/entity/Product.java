package lumi.insert.app.core.entity;


import java.util.ArrayList;
import java.util.List;
 
import org.hibernate.annotations.JdbcTypeCode; 
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.type.SqlTypes;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lumi.insert.app.core.entity.nondatabase.BaseAuditing;

@Entity(name = "products")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Audited
public class Product extends BaseAuditing {
    
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)    
    private Long basePrice;

    @Column(nullable = false)
    private Long sellPrice;

    @Column(nullable = false)
    private Long stockQuantity;

    @Builder.Default
    private Long stockMinimum = 0L;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    @NotAudited
    private Category category;

    @Builder.Default
    private Boolean isActive = true;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @NotAudited 
    @Builder.Default
    private List<String> pictureUrl = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    @Builder.Default
    @ToString.Exclude
    @NotAudited
    private List<ProductPicture> productPicture = new ArrayList<>();
}
