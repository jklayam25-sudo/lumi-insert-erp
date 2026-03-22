package lumi.insert.app.controller.supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.dto.request.SupplierUpdateRequest;
import lumi.insert.app.exception.DuplicateEntityException;
import lumi.insert.app.exception.NotFoundEntityException;

public class SupplierControllerUpdateTest extends BaseSupplierControllerTest{
    
    @Test
    void updateSupplierAPI_validRequest_returnUpdatedDTO() throws Exception{
        when(supplierService.updateSupplier(any(UUID.class), any(SupplierUpdateRequest.class))).thenReturn(supplierDetailResponse);

        mockMvc.perform(
            patch("/api/suppliers/" + supplierDetailResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("isActive", "false")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(supplierDetailResponse.id().toString()));

        verify(supplierService, times(1)).updateSupplier(eq(supplierDetailResponse.id()), argThat(arg -> arg.getIsActive().equals(false)));
    }

    @Test
    @WithMockUser(username = "admin", roles = "FINANCE")
    void updateSupplierAPI_anyRoleFINANCE_returnUpdatedDTO() throws Exception{
        when(supplierService.updateSupplier(any(UUID.class), any(SupplierUpdateRequest.class))).thenReturn(supplierDetailResponse);

        mockMvc.perform(
            patch("/api/suppliers/" + supplierDetailResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("isActive", "false")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(supplierDetailResponse.id().toString()));

        verify(supplierService, times(1)).updateSupplier(eq(supplierDetailResponse.id()), argThat(arg -> arg.getIsActive().equals(false)));
    }

    @Test
    @WithMockUser(username = "admin", roles = "OWNER")
    void updateSupplierAPI_higherRole_returnUpdatedDTO() throws Exception{
        when(supplierService.updateSupplier(any(UUID.class), any(SupplierUpdateRequest.class))).thenReturn(supplierDetailResponse);

        mockMvc.perform(
            patch("/api/suppliers/" + supplierDetailResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("isActive", "false")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(supplierDetailResponse.id().toString()));

        verify(supplierService, times(1)).updateSupplier(eq(supplierDetailResponse.id()), argThat(arg -> arg.getIsActive().equals(false)));
    }

    @Test
    @WithMockUser(username = "admin", roles = "CASHIER")
    void updateSupplierAPI_invalidRole_returnUpdatedDTO() throws Exception{
        when(supplierService.updateSupplier(any(UUID.class), any(SupplierUpdateRequest.class))).thenReturn(supplierDetailResponse);

        mockMvc.perform(
            patch("/api/suppliers/" + supplierDetailResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("isActive", "false")
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority"));  
    }

    @Test
    void updateSupplierAPI_duplicateName_returnConflict() throws Exception{
        when(supplierService.updateSupplier(any(UUID.class), any(SupplierUpdateRequest.class))).thenThrow(new DuplicateEntityException("Supplier with name " + supplierDetailResponse.name() + " already exists"));

        mockMvc.perform(
            patch("/api/suppliers/" + supplierDetailResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", supplierDetailResponse.name())
        )
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("errors").value("Supplier with name " + supplierDetailResponse.name() + " already exists"))
        .andExpect(jsonPath("$.data").isEmpty());

        verify(supplierService, times(1)).updateSupplier(eq(supplierDetailResponse.id()), argThat(arg -> arg.getName().equals(supplierDetailResponse.name())));
    }

    @Test
    void updateSupplierAPI_notFoundEntity_returnNotFound() throws Exception{
        when(supplierService.updateSupplier(any(UUID.class), any(SupplierUpdateRequest.class))).thenThrow(new NotFoundEntityException("Supplier with id " + supplierDetailResponse.id() + " is not found"));

        mockMvc.perform(
            patch("/api/suppliers/" + supplierDetailResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", supplierDetailResponse.name())
        )
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("errors").value("Supplier with id " + supplierDetailResponse.id() + " is not found"))
        .andExpect(jsonPath("$.data").isEmpty());

        verify(supplierService, times(1)).updateSupplier(eq(supplierDetailResponse.id()), argThat(arg -> arg.getName().equals(supplierDetailResponse.name())));
    }

}
