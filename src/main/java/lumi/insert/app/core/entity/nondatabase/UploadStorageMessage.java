package lumi.insert.app.core.entity.nondatabase;

import java.util.UUID;

import org.slf4j.MDC;

public record UploadStorageMessage (EntityList entity, UUID id, String path, EmployeeLogin auth, String requestId){ 
    public UploadStorageMessage (EntityList entity, UUID id, String path, EmployeeLogin auth){
        this(entity, id, path, auth, MDC.get("requestId"));
    };
}
