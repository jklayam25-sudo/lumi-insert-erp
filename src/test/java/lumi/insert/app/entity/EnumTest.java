package lumi.insert.app.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
 

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
 
import lumi.insert.app.core.entity.nondatabase.ProductSortOrder;
import lumi.insert.app.core.entity.nondatabase.TransactionSortOrder;

@ActiveProfiles("test")  
public class EnumTest {
     

    @Test
    void productSortOrder_createEnum_validType(){
        ProductSortOrder valueOf = ProductSortOrder.valueOf("createdAt");
        assertEquals(ProductSortOrder.createdAt, valueOf);
    }

    @Test
    void transactionSortOrder_createEnum_validType(){
        TransactionSortOrder valueOf = TransactionSortOrder.valueOf("createdAt");
        assertEquals(TransactionSortOrder.createdAt, valueOf);
    } 

}
