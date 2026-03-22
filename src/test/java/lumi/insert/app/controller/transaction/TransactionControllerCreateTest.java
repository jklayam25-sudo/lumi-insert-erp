package lumi.insert.app.controller.transaction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.dto.request.TransactionCreateRequest; 

public class TransactionControllerCreateTest extends BaseTransactionControllerTest{
    
    @Test
    @DisplayName("should return Transaction Response when create succesfully")
    public void createTransactionAPI_validRequest_shouldReturnCreatedEntity() throws Exception{
        when(transactionService.createTransaction(any(TransactionCreateRequest.class))).thenReturn(transactionResponse);
        mockMvc.perform(
            post("/api/transactions")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("customerId", UUID.randomUUID().toString())
            .param("staffId", UUID.randomUUID().toString()) 
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.invoiceId").value(transactionResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @WithMockUser(username = "admin", roles = "OWNER")
    public void createTransactionAPI_higherRole_shouldReturnCreatedEntity() throws Exception{
        when(transactionService.createTransaction(any(TransactionCreateRequest.class))).thenReturn(transactionResponse);
        mockMvc.perform(
            post("/api/transactions")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("customerId", UUID.randomUUID().toString())
            .param("staffId", UUID.randomUUID().toString()) 
        )
        .andDo(print()) 
        .andExpect(jsonPath("$.data.invoiceId").value(transactionResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @WithMockUser(username = "admin", roles = "WAREHOUSE")
    public void createTransactionAPI_invalidRole_shouldReturnCreatedEntity() throws Exception{
        when(transactionService.createTransaction(any(TransactionCreateRequest.class))).thenReturn(transactionResponse);
        mockMvc.perform(
            post("/api/transactions")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("customerId", UUID.randomUUID().toString())
            .param("staffId", UUID.randomUUID().toString()) 
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority"));
    }

    @Test
    @DisplayName("should return errors NotNull when request param is not valid")
    public void createTransactionAPI_nullCustomerId_shouldReturnNotNullError() throws Exception{
        when(transactionService.createTransaction(any(TransactionCreateRequest.class))).thenReturn(transactionResponse);
        mockMvc.perform(
            post("/api/transactions")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("staffId", UUID.randomUUID().toString()) 
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").value("customerId cannot be empty"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("should return errors NotNull when request param is not valid")
    public void createTransactionAPI_emptyWhiteSpace_shouldReturnNotNullError() throws Exception{
        when(transactionService.createTransaction(any(TransactionCreateRequest.class))).thenReturn(transactionResponse);
        mockMvc.perform(
            post("/api/transactions")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("customerId", " ")
            .param("staffId", UUID.randomUUID().toString()) 
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").isNotEmpty())
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("should return errors NotNull when request param is not valid")
    public void createTransactionAPI_missMatchType_shouldReturnNotNullError() throws Exception{
        when(transactionService.createTransaction(any(TransactionCreateRequest.class))).thenReturn(transactionResponse);
        mockMvc.perform(
            post("/api/transactions")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("customerId", "3")
            .param("staffId", UUID.randomUUID().toString()) 
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").isNotEmpty())
        .andExpect(jsonPath("$.data").isEmpty());
    }
}
