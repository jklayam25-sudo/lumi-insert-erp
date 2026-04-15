package lumi.insert.app.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import lumi.insert.app.core.entity.Category;

@ActiveProfiles("test") 
public class CategoryTest {
    
    @Test
    public void testCreateCategory() {
        Category dumpCategory = Category.builder()
        .name("Shoes")
        .build();

        assertNotNull(dumpCategory);
        assertEquals("Shoes", dumpCategory.getName());
    }

}
