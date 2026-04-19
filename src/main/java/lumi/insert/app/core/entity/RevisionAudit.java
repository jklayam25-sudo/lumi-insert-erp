package lumi.insert.app.core.entity;
 
import org.hibernate.envers.RevisionEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
 
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lumi.insert.app.config.RevisionListenerImpl;

/**
 * Representation class of database table {@code"revinfo"}. 
 * <p>Custom revision audit from envers, implement username and ipAddress for tracing purposes<br>
 * Class automatically implemented by{@code Spring Envers}. </p>
 * 
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Entity
@RevisionEntity(RevisionListenerImpl.class) 
@Table(name = "revinfo")
@Data 
public class RevisionAudit implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    @Column(name = "rev", nullable = false)
    private Long id;

    @RevisionTimestamp
    @Column(name = "revtstmp" , nullable = false) 
    private LocalDateTime timestamp;
  
    @Column(name = "username" , nullable = false) 
    private String username;

    @Column(name = "ip_address" , nullable = false) 
    private String ipAddress;
}
