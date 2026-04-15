package lumi.insert.app.utils.forTesting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import lumi.insert.app.core.entity.Category;
import lumi.insert.app.core.entity.Product;

public class ProductUtils {

    public static Slice<Product> getMockSliceProduct(){
        List<Product> products = new ArrayList<Product>();

        for ( int i = 1; i <= 12; i++ ) {
            final Long ids = Long.valueOf(i);
            Product dumpProduct = Product.builder()
            .id(ids)
            .name("Product " + i)
            .basePrice(BigDecimal.valueOf(1000L * i))
            .sellPrice(BigDecimal.valueOf(1200L * i))
            .stockQuantity(BigDecimal.valueOf(10L * i))
            .stockMinimum(BigDecimal.valueOf(1L * i))
            .build();

            products.add(dumpProduct);
        }

        Slice<Product> productSlice = new SliceImpl<>(products);

        return productSlice;
    }

    public static Product getMockCategorizedProduct(){
        Category dumpCategory = Category.builder()
        .id(1L)
        .name("Category")
        .isActive(true)
        .totalItems(0L)
        .build();

        Product dumpProduct = Product.builder()
            .id(1L)
            .name("Product")
            .basePrice(BigDecimal.valueOf(1000L))
            .sellPrice(BigDecimal.valueOf(1200L))
            .stockQuantity(BigDecimal.valueOf(10L))
            .stockMinimum(BigDecimal.valueOf(1L))
            .category(dumpCategory)
            .build();

        return dumpProduct;
    }

}
