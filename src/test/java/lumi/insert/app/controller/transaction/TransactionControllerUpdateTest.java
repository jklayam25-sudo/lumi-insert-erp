package lumi.insert.app.controller.transaction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

import lumi.insert.app.exception.ForbiddenRequestException;
import lumi.insert.app.exception.NotFoundEntityException;
 

public class TransactionControllerUpdateTest extends BaseTransactionControllerTest{
    
    @Test
    @DisplayName("should return updated Transaction Response when set to process succesfully")
    public void processTransactionAPI_validTransactionFromPending_shouldReturnCreatedEntity() throws Exception{
        when(transactionService.setTransactionToProcess(transactionResponse.id())).thenReturn(transactionResponse);
        mockMvc.perform(
            post("/api/transactions/" + transactionResponse.id() + "/process")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.invoiceId").value(transactionResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty());
        verify(transactionService, times(1)).setTransactionToProcess(transactionResponse.id());
    }

    @Test 
    @WithMockUser(username = "admin", roles = "OWNER")
    public void processTransactionAPI_higherRole_shouldReturnCreatedEntity() throws Exception{
        when(transactionService.setTransactionToProcess(transactionResponse.id())).thenReturn(transactionResponse);
        mockMvc.perform(
            post("/api/transactions/" + transactionResponse.id() + "/process")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.invoiceId").value(transactionResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty()); 
    }

    @Test 
    @WithMockUser(username = "admin", roles = "WAREHOUSE")
    public void processTransactionAPI_invalidRole_shouldReturnCreatedEntity() throws Exception{
        when(transactionService.setTransactionToProcess(transactionResponse.id())).thenReturn(transactionResponse);
        mockMvc.perform(
            post("/api/transactions/" + transactionResponse.id() + "/process")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority")); 
    }

    @Test
    @DisplayName("should return errors of invalid method when request Trx id is not UUID")
    public void processTransactionAPI_missMatchId_shouldReturnErrors() throws Exception{
        mockMvc.perform(
            post("/api/transactions/" + true + "/process")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("should return errors of notFoundEntity when request Trx id isn't valid")
    public void processTransactionAPI_invalidId_shouldReturnErrors() throws Exception{
        when(transactionService.setTransactionToProcess(any(UUID.class))).thenThrow(new NotFoundEntityException("Transaction with ID " + transactionResponse.id() + 1 + " was not found"));
        mockMvc.perform(
            post("/api/transactions/" + transactionResponse.id() + "/process")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Transaction with ID " + transactionResponse.id() + 1 + " was not found"));
    }

    @Test
    @DisplayName("should return errors of ForbiddenRequest when request Trx is not pending > only pending trx allowe to process")
    public void processTransactionAPI_transactionNotPending_shouldReturnErrors() throws Exception{
        when(transactionService.setTransactionToProcess(any(UUID.class))).thenThrow(new ForbiddenRequestException("Unable to process transaction because Transaction Status is not PENDING(CART)"));
        mockMvc.perform(
            post("/api/transactions/" + transactionResponse.id() + "/process")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Unable to process transaction because Transaction Status is not PENDING(CART)"));
    }

    @Test
    @DisplayName("should return Transaction Response when cancel transaction success")
    public void cancelTransactionAPI_validTransactionFromProcessOrComplete_shouldReturnEntity() throws Exception{
        when(transactionService.cancelTransaction(transactionResponse.id())).thenReturn(transactionResponse);
        mockMvc.perform(
            post("/api/transactions/" + transactionResponse.id() + "/cancel")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.invoiceId").value(transactionResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty());

        verify(transactionService, times(1)).cancelTransaction(transactionResponse.id());
    }

    @Test
    @WithMockUser(username = "admin", roles = "OWNER")
    @DisplayName("should return Transaction Response when cancel transaction success")
    public void cancelTransactionAPI_higherRole_shouldReturnEntity() throws Exception{
        when(transactionService.cancelTransaction(transactionResponse.id())).thenReturn(transactionResponse);
        mockMvc.perform(
            post("/api/transactions/" + transactionResponse.id() + "/cancel")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.invoiceId").value(transactionResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty());

        verify(transactionService, times(1)).cancelTransaction(transactionResponse.id());
    }

    @Test
    @WithMockUser(username = "admin", roles = "WAREHOUSE")
    @DisplayName("should return Transaction Response when cancel transaction success")
    public void cancelTransactionAPI_invalidRole_shouldReturnEntity() throws Exception{
        when(transactionService.cancelTransaction(transactionResponse.id())).thenReturn(transactionResponse);
        mockMvc.perform(
            post("/api/transactions/" + transactionResponse.id() + "/cancel")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority"));
    }

     @Test
    @DisplayName("should return errors of notFoundEntity when request Trx id isn't valid")
    public void cancelTransactionAPI_invalidId_shouldReturnErrors() throws Exception{
        when(transactionService.cancelTransaction(any(UUID.class))).thenThrow(new NotFoundEntityException("Transaction with ID " + transactionResponse.id() + 1 + " was not found"));
        mockMvc.perform(
            post("/api/transactions/" + transactionResponse.id() + "/cancel")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Transaction with ID " + transactionResponse.id() + 1 + " was not found"));
        verify(transactionService, times(1)).cancelTransaction(transactionResponse.id());
    }

    @Test
    @DisplayName("should return errors of ForbiddenRequest when request Trx is not pending > only procced or completed trx allowed to cancel")
    public void cancelTransactionAPI_transactionNotProcessOrComplete_shouldReturnErrors() throws Exception{
        when(transactionService.cancelTransaction(any(UUID.class))).thenThrow(new ForbiddenRequestException("Unable to cancel transaction because Transaction Status is not PROCESS OR COMPLETE"));
        mockMvc.perform(
            post("/api/transactions/" + transactionResponse.id() + "/cancel")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Unable to cancel transaction because Transaction Status is not PROCESS OR COMPLETE"));
        verify(transactionService, times(1)).cancelTransaction(transactionResponse.id());
    }

    @Test
    @DisplayName("should return Transaction Response when refresh transaction success")
    public void refreshTransactionAPI_validTransactionFromProcessOrComplete_shouldReturnEntity() throws Exception{
        when(transactionService.refreshTransaction(transactionResponse.id())).thenReturn(transactionResponse);
        mockMvc.perform(
            post("/api/transactions/" + transactionResponse.id() + "/refresh")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.invoiceId").value(transactionResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty());

        verify(transactionService, times(1)).refreshTransaction(transactionResponse.id());
    }

    @Test
    @WithMockUser(username = "admin", roles = "OWNER") 
    public void refreshTransactionAPI_higherRole_shouldReturnEntity() throws Exception{
        when(transactionService.refreshTransaction(transactionResponse.id())).thenReturn(transactionResponse);
        mockMvc.perform(
            post("/api/transactions/" + transactionResponse.id() + "/refresh")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.invoiceId").value(transactionResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty());

        verify(transactionService, times(1)).refreshTransaction(transactionResponse.id());
    }

    @Test 
    @WithMockUser(username = "admin", roles = "WAREHOUSE")
    public void refreshTransactionAPI_invalidRole_shouldReturnEntity() throws Exception{
        when(transactionService.refreshTransaction(transactionResponse.id())).thenReturn(transactionResponse);
        mockMvc.perform(
            post("/api/transactions/" + transactionResponse.id() + "/refresh")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority"));
    }

     @Test
    @DisplayName("should return errors of notFoundEntity when request Trx id isn't valid")
    public void refreshTransactionAPI_invalidId_shouldReturnErrors() throws Exception{
        when(transactionService.refreshTransaction(any(UUID.class))).thenThrow(new NotFoundEntityException("Transaction with ID " + transactionResponse.id() + 1 + " was not found"));
        mockMvc.perform(
            post("/api/transactions/" + transactionResponse.id() + "/refresh")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Transaction with ID " + transactionResponse.id() + 1 + " was not found"));
        verify(transactionService, times(1)).refreshTransaction(transactionResponse.id());
    }

    @Test
    @DisplayName("should return errors of ForbiddenRequest when request Trx is not pending < only pending / cart transaction is allowed to refresh")
    public void refreshTransactionAPI_transactionNotPending_shouldReturnErrors() throws Exception{
        when(transactionService.refreshTransaction(any(UUID.class))).thenThrow(new ForbiddenRequestException("Unable to refresh transaction because Transaction Status is not PENDING(CART)"));
        mockMvc.perform(
            post("/api/transactions/" + transactionResponse.id() + "/refresh")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Unable to refresh transaction because Transaction Status is not PENDING(CART)"));
        verify(transactionService, times(1)).refreshTransaction(transactionResponse.id());
    }

}
