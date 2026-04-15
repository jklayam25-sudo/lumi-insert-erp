package lumi.insert.app.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import lumi.insert.app.core.entity.Category;
import lumi.insert.app.core.entity.Product;

@ActiveProfiles("test")  
public class ProductTest {
    
    @Test
    public void testCreateProduct() {
        Product dumpProduct = Product.builder()
        .name("NIKE Jordan Low 3")
        .basePrice(BigDecimal.valueOf(10000L))
        .sellPrice(BigDecimal.valueOf(12000L))
        .stockQuantity(BigDecimal.valueOf(50L))
        .stockMinimum(BigDecimal.valueOf(5L))
        .build();

        assertNotNull(dumpProduct);
        assertEquals("NIKE Jordan Low 3", dumpProduct.getName());
        assertTrue(BigDecimal.valueOf(10000L).compareTo(dumpProduct.getBasePrice()) == 0);
        assertTrue(BigDecimal.valueOf(12000L).compareTo(dumpProduct.getSellPrice()) == 0);
        assertTrue(BigDecimal.valueOf(50L).compareTo(dumpProduct.getStockQuantity()) == 0);
        assertTrue(BigDecimal.valueOf(5L).compareTo(dumpProduct.getStockMinimum()) == 0);
    }

    @Test
    public void testCreateProductWithCategory() {
        Category dumpCategory = Category.builder()
        .name("Shoes")
        .build();

        Product dumpProduct = Product.builder()
        .name("NIKE Jordan Low 3")
        .basePrice(BigDecimal.valueOf(10000L))
        .sellPrice(BigDecimal.valueOf(12000L))
        .stockQuantity(BigDecimal.valueOf(50L))
        .stockMinimum(BigDecimal.valueOf(5L))
        .category(dumpCategory)
        .build();

        assertNotNull(dumpProduct);
        assertSame(dumpCategory, dumpProduct.getCategory());
        assertEquals("NIKE Jordan Low 3", dumpProduct.getName());
        assertTrue(BigDecimal.valueOf(10000L).compareTo(dumpProduct.getBasePrice()) == 0);
        assertTrue(BigDecimal.valueOf(12000L).compareTo(dumpProduct.getSellPrice()) == 0);
        assertTrue(BigDecimal.valueOf(50L).compareTo(dumpProduct.getStockQuantity()) == 0);
        assertTrue(BigDecimal.valueOf(5L).compareTo(dumpProduct.getStockMinimum()) == 0);
    }

}
