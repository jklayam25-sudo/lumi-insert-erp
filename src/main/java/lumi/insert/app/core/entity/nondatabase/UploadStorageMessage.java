package lumi.insert.app.core.entity.nondatabase;

import java.util.UUID;

public record UploadStorageMessage (EntityList entity, UUID id, String path, EmployeeLogin auth){ 
}
