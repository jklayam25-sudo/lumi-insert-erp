package lumi.insert.app.activitycore.entity.nondatabase;

import org.slf4j.MDC;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lumi.insert.app.activitycore.entity.ActivityLog;
  
@EqualsAndHashCode(callSuper = false)
@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class ActivityLogMessage extends ActivityLog{
    
    private String requestId;

    public ActivityLogMessage(ActivityLog log){
        super(log.getId(), log.getEntityName(), log.getEntityId(), log.getAction(), log.getActionMessage(), log.getIpAddress());
        this.requestId = MDC.get("requestId");
    }
}
