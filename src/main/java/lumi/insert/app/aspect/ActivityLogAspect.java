package lumi.insert.app.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.stereotype.Component;

import com.github.f4b6a3.uuid.UuidCreator;

import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.activitycore.entity.ActivityLog;
import lumi.insert.app.aspect.annotation.ActivityLogger;
import lumi.insert.app.config.security.AuditorAwareImpl;
import lumi.insert.app.dto.response.Identifiable;

import lumi.insert.app.service.MessageProducerService;

@Aspect
@Component
@Slf4j
public class ActivityLogAspect {

    @Autowired
    MessageProducerService messageProducerService;

    @Autowired
    AuditorAwareImpl auditorAwareImpl;
    
    @AfterReturning(
        pointcut = "@annotation(activityLog)",
        returning = "response"
    )
    void afterMethod(JoinPoint joinPoint, ActivityLogger activityLog, Object response){ 
        ActivityLog result = ActivityLog.builder()
            .id(UuidCreator.getTimeOrderedEpochFast())
            .action(activityLog.action())
            .actionMessage(activityLog.actionMessage())
            .entityName(activityLog.entityName()) 
            .ipAddress(auditorAwareImpl.getAuditorIpAddress().get())
            .build();
            
            result.setCreatedBy(auditorAwareImpl.getCurrentAuditor().get());
            result.setUpdatedBy(auditorAwareImpl.getCurrentAuditor().get());

        if (activityLog.entityIdFromSingleParam()){
            Object[] args = joinPoint.getArgs();
            if(args[0] instanceof Identifiable){
                Identifiable idGetter = ((Identifiable) args[0]);
                result.setEntityId(idGetter.getId());
            }
            log.info(result.getEntityId());
        }

        if(response instanceof Identifiable){
            result.setEntityId(((Identifiable) response).getId());
        }

        messageProducerService.sendActivityLog(result);

    }

}
