package lumi.insert.app.controller.customer;

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

import lumi.insert.app.dto.request.CustomerUpdateRequest;
import lumi.insert.app.exception.DuplicateEntityException;
import lumi.insert.app.exception.NotFoundEntityException;

public class CustomerControllerUpdateTest extends BaseCustomerControllerTest{
    
    @Test
    void updateCustomerAPI_validRequest_returnUpdatedDTO() throws Exception{
        when(customerService.updateCustomer(any(UUID.class), any(CustomerUpdateRequest.class))).thenReturn(customerDetailResponse);

        mockMvc.perform(
            patch("/api/customers/" + customerDetailResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("isActive", "false")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(customerDetailResponse.id().toString()));

        verify(customerService, times(1)).updateCustomer(eq(customerDetailResponse.id()), argThat(arg -> arg.getIsActive().equals(false)));
    }

    @Test
    @WithMockUser(username = "admin", roles = "FINANCE")
    void updateCustomerAPI_anyRoleFINANCE_returnUpdatedDTO() throws Exception{
        when(customerService.updateCustomer(any(UUID.class), any(CustomerUpdateRequest.class))).thenReturn(customerDetailResponse);

        mockMvc.perform(
            patch("/api/customers/" + customerDetailResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("isActive", "false")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(customerDetailResponse.id().toString()));

        verify(customerService, times(1)).updateCustomer(eq(customerDetailResponse.id()), argThat(arg -> arg.getIsActive().equals(false)));
    }

    @Test
    @WithMockUser(username = "admin", roles = "OWNER")
    void updateCustomerAPI_higherRole_returnUpdatedDTO() throws Exception{
        when(customerService.updateCustomer(any(UUID.class), any(CustomerUpdateRequest.class))).thenReturn(customerDetailResponse);

        mockMvc.perform(
            patch("/api/customers/" + customerDetailResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("isActive", "false")
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(customerDetailResponse.id().toString()));

        verify(customerService, times(1)).updateCustomer(eq(customerDetailResponse.id()), argThat(arg -> arg.getIsActive().equals(false)));
    }

    @Test 
    @WithMockUser(username = "admin", roles = "WAREHOUSE")
    void createCustomerAPI_invalidRole_returnDTO() throws Exception{  
        mockMvc.perform(
            patch("/api/customers/" + customerDetailResponse.id())
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
    void updateCustomerAPI_duplicateName_returnConflict() throws Exception{
        when(customerService.updateCustomer(any(UUID.class), any(CustomerUpdateRequest.class))).thenThrow(new DuplicateEntityException("Customer with name " + customerDetailResponse.name() + " already exists"));

        mockMvc.perform(
            patch("/api/customers/" + customerDetailResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", customerDetailResponse.name())
        )
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("errors").value("Customer with name " + customerDetailResponse.name() + " already exists"))
        .andExpect(jsonPath("$.data").isEmpty());

        verify(customerService, times(1)).updateCustomer(eq(customerDetailResponse.id()), argThat(arg -> arg.getName().equals(customerDetailResponse.name())));
    }

    @Test
    void updateCustomerAPI_notFoundEntity_returnNotFound() throws Exception{
        when(customerService.updateCustomer(any(UUID.class), any(CustomerUpdateRequest.class))).thenThrow(new NotFoundEntityException("Customer with id " + customerDetailResponse.id() + " is not found"));

        mockMvc.perform(
            patch("/api/customers/" + customerDetailResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", customerDetailResponse.name())
        )
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("errors").value("Customer with id " + customerDetailResponse.id() + " is not found"))
        .andExpect(jsonPath("$.data").isEmpty());

        verify(customerService, times(1)).updateCustomer(eq(customerDetailResponse.id()), argThat(arg -> arg.getName().equals(customerDetailResponse.name())));
    }

}
