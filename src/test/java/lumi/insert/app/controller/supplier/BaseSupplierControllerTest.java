package lumi.insert.app.controller.supplier;

import java.util.List;
import java.util.UUID;
 
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.controller.BaseControllerTest;
import lumi.insert.app.dto.response.SupplierDetailResponse;
import lumi.insert.app.dto.response.SupplierNameResponse;  
 
@WithMockUser(username = "admin", roles = "WAREHOUSE")
public abstract class BaseSupplierControllerTest extends BaseControllerTest { 

    SupplierDetailResponse supplierDetailResponse = new SupplierDetailResponse(UUID.randomUUID(), "Test LTE.", "test@gmail.com", "Test - 00xxx", null, null, null, null);

    Slice<SupplierDetailResponse> sliceSupplierResponse = new SliceImpl<>(List.of(supplierDetailResponse));

    Slice<SupplierNameResponse> sliceNames = new SliceImpl<>(List.of(new SupplierNameResponse(supplierDetailResponse.id(), supplierDetailResponse.name())));
 
}
