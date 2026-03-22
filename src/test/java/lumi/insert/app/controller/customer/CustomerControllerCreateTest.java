package lumi.insert.app.controller.customer;

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

import lumi.insert.app.dto.request.CustomerCreateRequest;
import lumi.insert.app.exception.DuplicateEntityException;

public class CustomerControllerCreateTest extends BaseCustomerControllerTest{
    
    @Test 
    void createCustomerAPI_validRequest_returnDTO() throws Exception{
        when(customerService.createCustomer(any(CustomerCreateRequest.class))).thenReturn(customerDetailResponse);

        mockMvc.perform(
            post("/api/customers")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", customerDetailResponse.name())
            .param("email", customerDetailResponse.email())
            .param("contact", customerDetailResponse.contact())
            .param("shippingAddress", customerDetailResponse.shippingAddress())
        )
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(customerDetailResponse.id().toString()))
        .andExpect(jsonPath("$.errors").isEmpty());

        verify(customerService, times(1)).createCustomer(argThat(arg -> arg.getShippingAddress().equals(customerDetailResponse.shippingAddress())));
    }

    @Test 
    @WithMockUser(username = "admin", roles = "FINANCE")
    void createCustomerAPI_anyRoleFINANCE_returnDTO() throws Exception{
        when(customerService.createCustomer(any(CustomerCreateRequest.class))).thenReturn(customerDetailResponse);

        mockMvc.perform(
            post("/api/customers")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", customerDetailResponse.name())
            .param("email", customerDetailResponse.email())
            .param("contact", customerDetailResponse.contact())
            .param("shippingAddress", customerDetailResponse.shippingAddress())
        )
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(customerDetailResponse.id().toString()))
        .andExpect(jsonPath("$.errors").isEmpty());

        verify(customerService, times(1)).createCustomer(argThat(arg -> arg.getShippingAddress().equals(customerDetailResponse.shippingAddress())));
    }

    @Test 
    @WithMockUser(username = "admin", roles = "OWNER")
    void createCustomerAPI_higherRole_returnDTO() throws Exception{
        when(customerService.createCustomer(any(CustomerCreateRequest.class))).thenReturn(customerDetailResponse);

        mockMvc.perform(
            post("/api/customers")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", customerDetailResponse.name())
            .param("email", customerDetailResponse.email())
            .param("contact", customerDetailResponse.contact())
            .param("shippingAddress", customerDetailResponse.shippingAddress())
        )
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(customerDetailResponse.id().toString()))
        .andExpect(jsonPath("$.errors").isEmpty());

        verify(customerService, times(1)).createCustomer(argThat(arg -> arg.getShippingAddress().equals(customerDetailResponse.shippingAddress())));
    }

    @Test 
    @WithMockUser(username = "admin", roles = "WAREHOUSE")
    void createCustomerAPI_invalidRole_returnDTO() throws Exception{  
        mockMvc.perform(
            post("/api/customers")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", customerDetailResponse.name())
            .param("email", customerDetailResponse.email())
            .param("contact", customerDetailResponse.contact())
            .param("shippingAddress", customerDetailResponse.shippingAddress())
        )
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority")); 
    }

    @Test 
    void createCustomerAPI_invalidEmail_returnBadrequest() throws Exception{ 

        mockMvc.perform(
            post("/api/customers")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", customerDetailResponse.name())
            .param("email", "notemail")
            .param("contact", customerDetailResponse.contact())
            .param("shippingAddress", customerDetailResponse.shippingAddress())
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("email doesn't meet the email format"));
    }

    @Test 
    void createCustomerAPI_invalidName_returnBadrequest() throws Exception{ 
        mockMvc.perform(
            post("/api/customers")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", "test")
            .param("email", customerDetailResponse.email())
            .param("contact", customerDetailResponse.contact())
            .param("shippingAddress", customerDetailResponse.shippingAddress())
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("name has to be 5-30 length"));
    }
    @Test 
    void createCustomerAPI_duplicateName_returnDTO() throws Exception{
        when(customerService.createCustomer(any(CustomerCreateRequest.class))).thenThrow(new DuplicateEntityException("Customer with name " + customerDetailResponse.name() + " already exists"));

        mockMvc.perform(
            post("/api/customers")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .param("name", customerDetailResponse.name())
            .param("email", customerDetailResponse.email())
            .param("contact", customerDetailResponse.contact())
            .param("shippingAddress", customerDetailResponse.shippingAddress())
        )
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Customer with name " + customerDetailResponse.name() + " already exists"));

        verify(customerService, times(1)).createCustomer(argThat(arg -> arg.getShippingAddress().equals(customerDetailResponse.shippingAddress())));
    }
}
