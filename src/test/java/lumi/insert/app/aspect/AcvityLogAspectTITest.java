package lumi.insert.app.aspect;
 
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
 
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import; 
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.transaction.Transactional;
import lumi.insert.app.TestContainerTest;
import lumi.insert.app.config.security.AuditorAwareImpl;
import lumi.insert.app.core.entity.nondatabase.ActivityAction; 
import lumi.insert.app.dto.request.CategoryCreateRequest;
import lumi.insert.app.dto.response.CategoryResponse;
import lumi.insert.app.service.CategoryService; 
import lumi.insert.app.service.implement.MessageProducerServiceImpl; 

@SpringBootTest 
@Import(ActivityLogAspect.class)
@ActiveProfiles("test") 
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Transactional
public class AcvityLogAspectTITest extends TestContainerTest{
    
    @Autowired
    CategoryService categoryService;

    @MockitoBean
    MessageProducerServiceImpl messageProducerService;

    @MockitoBean
    AuditorAwareImpl auditorAwareImpl;

    @DynamicPropertySource
    static void allowAOP(DynamicPropertyRegistry registry){
        registry.add("app.aop.enabled", () -> true);
    }
  
    @Test
    void saveCategory_shouldSaveActivityLog(){
        when(auditorAwareImpl.getCurrentAuditor()).thenReturn(Optional.of("SYSTEM"));
        when(auditorAwareImpl.getAuditorIpAddress()).thenReturn(Optional.of("0.1.2.3"));

        CategoryCreateRequest request = CategoryCreateRequest.builder()
        .name("testxxx")                
        .build();

        CategoryResponse response = categoryService.createCategory(request);

        verify(auditorAwareImpl, times(1)).getAuditorIpAddress();
        verify(messageProducerService, times(1)).sendActivityLog(argThat(arg -> arg.getEntityId().equals(String.valueOf(response.id())) && arg.getAction() == ActivityAction.CATEGORY_CREATED));
    }
} 



