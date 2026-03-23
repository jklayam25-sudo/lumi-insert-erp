package lumi.insert.app.aspect;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import lumi.insert.app.aspect.annotation.ActivityLogger;
import lumi.insert.app.config.security.AuditorAwareImpl;
import lumi.insert.app.core.entity.nondatabase.ActivityAction;
import lumi.insert.app.dto.response.ProductResponse;
import lumi.insert.app.service.MessageProducerService;

@ExtendWith(MockitoExtension.class) 
@ActiveProfiles("test")
public class ActivityLogAspectTest {
    
    @InjectMocks
    ActivityLogAspect activityLogAspect;

    @Mock
    MessageProducerService messageProducerService;

    @Mock
    AuditorAwareImpl auditorAwareImpl;

    @Mock
    ActivityLogger activityLogger;

    @Test
    void afterMethod_shouldSendActivityLog(){
        when(activityLogger.action()).thenReturn(ActivityAction.PRODUCT_CREATED);
        when(activityLogger.actionMessage()).thenReturn("a message");
        when(activityLogger.entityName()).thenReturn("products");
        when(auditorAwareImpl.getAuditorIpAddress()).thenReturn(Optional.of("0.1.2.3"));


        ProductResponse productResponse = new ProductResponse(1L, null, null, null, null, null, null, null, null);
        activityLogAspect.afterMethod(null, activityLogger, productResponse);

        verify(messageProducerService, times(1)).sendActivityLog(argThat(arg -> arg.getEntityId().equals(String.valueOf(1L)) && arg.getAction() == ActivityAction.PRODUCT_CREATED));
    }

}
