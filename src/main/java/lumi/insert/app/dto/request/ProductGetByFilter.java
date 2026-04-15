package lumi.insert.app.dto.request;
 
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Schema(description = "Filter request for searching products")
public class ProductGetByFilter extends PaginationRequest {

    @Builder.Default
    @Schema(description = "Filter product by name", example = "Shoes ")
    String name = "";

    @Builder.Default
    @Min(value = 0, message = "minPrice minimal value is 0")
    @Schema(description = "Minimum product price", example = "0")
    BigDecimal minPrice = BigDecimal.ZERO;

    @Builder.Default
    @Min(value = 0, message = "maxPrice minimal value is 0")
    @Schema(description = "Maximum product price", example = "1000000")
    BigDecimal maxPrice = BigDecimal.valueOf(999999999);
    
    @Schema(description = "Filter by category ID", example = "1")
    Long categoryId;

    @Builder.Default
    @Pattern(regexp = "createdAt|updatedAt|sellPrice|basePrice|stockQuantity", message = "check documentation for sortBy specification")
    String sortBy = "sellPrice";

    @Builder.Default
    @Pattern(regexp = "DESC|ASC", message = "check documentation for sortDirection specification")
    String sortDirection = "DESC";

}
