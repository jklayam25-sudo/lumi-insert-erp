package lumi.insert.app.entity.audited;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory; 
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest; 
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean; 
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager; 
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.TestContainerTest;
import lumi.insert.app.config.security.AuditorAwareImpl;
import lumi.insert.app.core.entity.Category; 
import lumi.insert.app.dto.request.CategoryCreateRequest;
import lumi.insert.app.dto.response.CategoryResponse;
import lumi.insert.app.service.CategoryService;

@SpringBootTest 
@Slf4j
@ActiveProfiles("test")
public class AuditedEntityTITest extends TestContainerTest {
    
    @Autowired
    CategoryService categoryService;
  
    @MockitoBean
    AuditorAwareImpl auditorAwareImpl;

    @Autowired
    EntityManager entityManager;

    @Autowired
    TransactionTemplate transactionTemplate; 
 
    @Test
    void createCategory_shouldCreateCategoryAud(){
        when(auditorAwareImpl.getCurrentAuditor()).thenReturn(Optional.of("SYSTEM"));
        when(auditorAwareImpl.getAuditorIpAddress()).thenReturn(Optional.of("0.1.2.3"));

        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    
        CategoryCreateRequest request = CategoryCreateRequest.builder()
            .name("testxxeerrx" + LocalDateTime.now())
            .build();

        CategoryResponse execute = transactionTemplate.execute(status -> {
            CategoryResponse response = categoryService.createCategory(request);  
            return response;
        });;
  
        Category result = transactionTemplate.execute(status -> {
            AuditReader auditReader = AuditReaderFactory.get(entityManager); 

            List<Number> revisions = auditReader.getRevisions(Category.class, execute.getId());
            assertFalse(revisions.isEmpty());
    
            Number lastRev = revisions.get(revisions.size() - 1);
            
            return auditReader.find(Category.class, execute.getId(), lastRev); 
        });
        
        assertEquals(request.getName(), result.getName()); 
    }

}
