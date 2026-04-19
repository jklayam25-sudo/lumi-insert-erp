package lumi.insert.app.core.entity;

import java.util.List;

import org.hibernate.envers.Audited;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lumi.insert.app.core.entity.nondatabase.BaseAuditing;
import lumi.insert.app.core.entity.nondatabase.EmployeeRole;

/**
 * Representation class of database table {@code"memos"}. 
 * <p>Store a persistent internal memorandum or notice..<br>
 * Class can be implemented by{@code NoArgsConstructor, AllArgsConstructor and Builder}. </p>
 * 
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Entity(name = "memos")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Audited
public class Memo extends BaseAuditing{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Brief summary or subject of the memo. 
     * Limited to 55 characters for UI display consistency.
     */
    @Column(nullable = false, length = 55)
    private String title;

    /**
     * The main content or message body of the memo.
     */
    @Column(nullable = false)
    private String body;

    /**
     * Collection of URLs or paths pointing to associated image attachments.
     * <p>Note: These are stored as a list of strings, typically handled via 
     * a converter or specific database column type.</p>
     */
    @Column(nullable = true)
    private List<String> images;

    /**
     * Flag to determine if the memo is currently visible to users.
     */
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Target audience for this memo based on their organizational role.
     * <p>If set to {@code null}, the memo is considered public or 
     * accessible by all roles.</p>
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EmployeeRole role = null;

}
