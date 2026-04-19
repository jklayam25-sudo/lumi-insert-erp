package lumi.insert.app.activitycore.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lumi.insert.app.core.entity.nondatabase.ActivityAction;
import lumi.insert.app.core.entity.nondatabase.BaseAuditing;

/**
 * Representation class of database table {@code"activity_logs"}. 
 * <p>Represent detailed activity logs.<br>
 * Class can be implemented by{@code NoArgsConstructor, AllArgsConstructor and Builder}. </p>
 * 
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Entity(name = "activity_logs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
public class ActivityLog extends BaseAuditing {
    
    @Id  
    private UUID id;

    @Column(name = "entity_name" , nullable = false) 
    private String entityName;

    @Column(name = "entity_id" , nullable = false) 
    private String entityId;

    @Enumerated(EnumType.STRING) 
    @Column(name = "action" , nullable = false) 
    private ActivityAction action;

    @Column(name = "action_message" , nullable = false) 
    private String actionMessage;

    @Column(name = "ip_address" , nullable = false) 
    private String ipAddress;
 
}
