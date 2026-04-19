package lumi.insert.app.core.entity;
 

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import jakarta.persistence.Column; 
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lumi.insert.app.core.entity.nondatabase.BaseAuditing;

/**
 * Representation class of database table {@code"memo_views"}. 
 * <p>Store viewed memo and employee that view <br>
 * Class can be implemented by{@code NoArgsConstructor, AllArgsConstructor and Builder}. </p>
 * 
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Entity(name = "memo_views")
@Table(
     uniqueConstraints = @UniqueConstraint(columnNames = {"memo_id", "employee_id"})
)
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Audited
public class MemoView extends BaseAuditing{

    @Id
    @Column(nullable = false)
    String id;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "memo_id", nullable = false) 
    private Memo memo;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "employee_id", nullable = false) 
    @NotAudited
    private Employee employee;

    /**
     * Default constructor, return MemoView with id from combining memo's id and employee's id.
     * @param memo
     * @param employee
     */
    public MemoView (Memo memo, Employee employee){
        this(memo.getId().toString() + employee.getId(), memo, employee);
    }

}
