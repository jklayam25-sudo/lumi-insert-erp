package lumi.insert.app.core.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated; 
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lumi.insert.app.core.entity.nondatabase.BaseAuditing;
import lumi.insert.app.core.entity.nondatabase.EmployeeRole;

@Entity(name = "employees")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data 
@Audited
public class Employee extends BaseAuditing{
    
    @Id 
    private UUID id;

    @Column(unique = true, nullable = false, length = 55) 
    private String username;

    @Column(nullable = false)
    private String fullname;

    @Column(nullable = false)
    @NotAudited
    private String password;

    @Column(nullable = false)
    @NotAudited
    private LocalDateTime joinDate;

    @Column(nullable = true)
    private String lastIp;
    
    @Column(nullable = true)
    private String lastDevice;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = true)
    @NotAudited
    private String pictureUrl;

    @NotAudited 
    @OneToOne(mappedBy = "employee")
    private EmployeePicture employeePicture;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EmployeeRole role = EmployeeRole.CASHIER;

}
