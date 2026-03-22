package lumi.insert.app.controller.supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.dto.request.SupplierCreateRequest; 
import lumi.insert.app.exception.DuplicateEntityException; 

public class SupplierControllerCreateTest extends BaseSupplierControllerTest{
    
    @Test 
    void createSupplierAPI_validRequest_returnDTO() throws Exception{
        when(supplierService.createSupplier(any(SupplierCreateRequest.class))).thenReturn(supplierDetailResponse);

        mockMvc.perform(
            post("/api/suppliers")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", supplierDetailResponse.name())
            .param("email", supplierDetailResponse.email())
            .param("contact", supplierDetailResponse.contact()) 
        )
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(supplierDetailResponse.id().toString()))
        .andExpect(jsonPath("$.errors").isEmpty());

        verify(supplierService, times(1)).createSupplier(argThat(arg -> arg.getContact().equals(supplierDetailResponse.contact())));
    }

    @Test 
    @WithMockUser(username = "admin", roles = "FINANCE")
    void createSupplierAPI_anyRoleFINANCE_returnDTO() throws Exception{
        when(supplierService.createSupplier(any(SupplierCreateRequest.class))).thenReturn(supplierDetailResponse);

        mockMvc.perform(
            post("/api/suppliers")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", supplierDetailResponse.name())
            .param("email", supplierDetailResponse.email())
            .param("contact", supplierDetailResponse.contact()) 
        )
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(supplierDetailResponse.id().toString()))
        .andExpect(jsonPath("$.errors").isEmpty()); 
    }

    @Test 
    @WithMockUser(username = "admin", roles = "OWNER")
    void createSupplierAPI_higherRole_returnDTO() throws Exception{
        when(supplierService.createSupplier(any(SupplierCreateRequest.class))).thenReturn(supplierDetailResponse);

        mockMvc.perform(
            post("/api/suppliers")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", supplierDetailResponse.name())
            .param("email", supplierDetailResponse.email())
            .param("contact", supplierDetailResponse.contact()) 
        )
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(supplierDetailResponse.id().toString()))
        .andExpect(jsonPath("$.errors").isEmpty()); 
    }

    @Test 
    @WithMockUser(username = "admin", roles = "CASHIER")
    void createSupplierAPI_invalidRole_returnDTO() throws Exception{
        when(supplierService.createSupplier(any(SupplierCreateRequest.class))).thenReturn(supplierDetailResponse);

        mockMvc.perform(
            post("/api/suppliers")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", supplierDetailResponse.name())
            .param("email", supplierDetailResponse.email())
            .param("contact", supplierDetailResponse.contact()) 
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority"));  
    }

    @Test 
    void createSupplierAPI_invalidEmail_returnBadrequest() throws Exception{ 

        mockMvc.perform(
            post("/api/suppliers")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", supplierDetailResponse.name())
            .param("email", "notemail")
            .param("contact", supplierDetailResponse.contact()) 
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("email doesn't meet the email format"));
    }

    @Test 
    void createSupplierAPI_invalidName_returnBadrequest() throws Exception{ 
        mockMvc.perform(
            post("/api/suppliers")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", "test")
            .param("email", supplierDetailResponse.email())
            .param("contact", supplierDetailResponse.contact()) 
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("name has to be 5-30 length"));
    }
    @Test 
    void createSupplierAPI_duplicateName_returnDTO() throws Exception{
        when(supplierService.createSupplier(any(SupplierCreateRequest.class))).thenThrow(new DuplicateEntityException("Supplier with name " + supplierDetailResponse.name() + " already exists"));

        mockMvc.perform(
            post("/api/suppliers")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", supplierDetailResponse.name())
            .param("email", supplierDetailResponse.email())
            .param("contact", supplierDetailResponse.contact()) 
        )
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Supplier with name " + supplierDetailResponse.name() + " already exists"));

        verify(supplierService, times(1)).createSupplier(argThat(arg -> arg.getContact().equals(supplierDetailResponse.contact())));
    }
}
