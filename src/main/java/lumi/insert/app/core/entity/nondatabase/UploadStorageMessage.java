package lumi.insert.app.core.entity.nondatabase;

import java.util.UUID;

import org.slf4j.MDC;

import lumi.insert.app.service.implement.MessageProducerServiceImpl;

/**
 * Wrapper broker message for {@link MessageProducerServiceImpl#sendUploadToStorage(UploadStorageMessage)}. 
 * 
 * @author KelvinKhodes
 * @since 1.0.0 
 */
public record UploadStorageMessage (EntityList entity, UUID id, String path, EmployeeLogin auth, String requestId){ 

    /**
     * Default constructor of this wrapper
     * <p>Assign MDC(Request ID) for trace and logging.</p>
     * @param entity
     * @param id
     * @param path
     * @param auth
     */
    public UploadStorageMessage (EntityList entity, UUID id, String path, EmployeeLogin auth){
        this(entity, id, path, auth, MDC.get("requestId"));
    };
}
