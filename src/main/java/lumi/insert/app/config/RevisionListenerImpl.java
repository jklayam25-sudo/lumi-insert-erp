package lumi.insert.app.config;

import org.hibernate.envers.RevisionListener;

import lumi.insert.app.config.security.AuditorAwareImpl;
import lumi.insert.app.core.entity.RevisionAudit;
 
/**
 * Custom {@link  RevisionListener}, added Username and IpAddress .
 * @author KelvinKhodes
 * @since 1.0.0 
 */
public class RevisionListenerImpl implements RevisionListener{
 
    private final AuditorAwareImpl auditorAwareImpl = new AuditorAwareImpl();

    @Override
    public void newRevision(Object entity) {
        if(entity instanceof RevisionAudit){
            RevisionAudit revisionAudit = (RevisionAudit) entity;
            revisionAudit.setUsername(auditorAwareImpl.getCurrentAuditor().orElse("SYSTEM"));
            revisionAudit.setIpAddress(auditorAwareImpl.getAuditorIpAddress().orElse("0.0.0.0"));
        }
    }
    
}
