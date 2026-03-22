package lumi.insert.app.controller.supply;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test; 
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import lumi.insert.app.dto.request.ItemRefundRequest; 
import lumi.insert.app.dto.request.SupplyUpdateRequest;
import lumi.insert.app.exception.ForbiddenRequestException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.exception.TransactionValidationException;
 

public class SupplyControllerUpdateTest extends BaseSupplyControllerTest{

    @Test
    @DisplayName("should return Supply Response when cancel supply success")
    public void cancelSupplyAPI_validSupplyFromProcessOrComplete_shouldReturnEntity() throws Exception{
        when(supplyService.cancelSupply(supplyResponse.id())).thenReturn(supplyResponse);
        mockMvc.perform(
            post("/api/supplies/" + supplyResponse.id() + "/cancel")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.invoiceId").value(supplyResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty());

        verify(supplyService, times(1)).cancelSupply(supplyResponse.id());
    }

    @Test 
    @WithMockUser(username = "admin", roles = "OWNER") 
    public void cancelSupplyAPI_higherRole_shouldReturnEntity() throws Exception{
        when(supplyService.cancelSupply(supplyResponse.id())).thenReturn(supplyResponse);
        mockMvc.perform(
            post("/api/supplies/" + supplyResponse.id() + "/cancel")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.invoiceId").value(supplyResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty()); 
    }

    @Test 
    @WithMockUser(username = "admin", roles = "FINANCE") 
    public void cancelSupplyAPI_invalidRole_shouldReturnEntity() throws Exception{
        when(supplyService.cancelSupply(supplyResponse.id())).thenReturn(supplyResponse);
        mockMvc.perform(
            post("/api/supplies/" + supplyResponse.id() + "/cancel")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority"));  
    }

    @Test
    @DisplayName("should return errors of notFoundEntity when request Trx id isn't valid")
    public void cancelSupplyAPI_invalidId_shouldReturnErrors() throws Exception{
        when(supplyService.cancelSupply(any(UUID.class))).thenThrow(new NotFoundEntityException("Supply with ID " + supplyResponse.id() + 1 + " was not found"));
        mockMvc.perform(
            post("/api/supplies/" + supplyResponse.id() + "/cancel")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Supply with ID " + supplyResponse.id() + 1 + " was not found"));
        verify(supplyService, times(1)).cancelSupply(supplyResponse.id());
    }

    @Test
    @DisplayName("should return errors of ForbiddenRequest when request Trx is not pending > only procced or completed trx allowed to cancel")
    public void cancelSupplyAPI_supplyAlreadyCancelled_shouldReturnErrors() throws Exception{
        when(supplyService.cancelSupply(any(UUID.class))).thenThrow(new ForbiddenRequestException("Unable to cancel supply because Supply Status is CANCELLED"));
        mockMvc.perform(
            post("/api/supplies/" + supplyResponse.id() + "/cancel")
            .accept(MediaType.APPLICATION_JSON_VALUE)   
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Unable to cancel supply because Supply Status is CANCELLED"));
        verify(supplyService, times(1)).cancelSupply(supplyResponse.id());
    }

    @Test
    @DisplayName("should return Supply Response when create succesfully")
    public void updateSupplyAPI_validRequest_shouldReturnUpdatedEntity() throws Exception{
        when(supplyService.updateSupply(any(), any(SupplyUpdateRequest.class))).thenReturn(supplyResponse);
        mockMvc.perform(
            patch("/api/supplies/" + supplyResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("invoiceId","INV NEW")  
            .param("totalFee","10")  
            
        )
        .andDo(print()) 
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.invoiceId").value(supplyResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty());

        verify(supplyService, times(1)).updateSupply(eq(supplyResponse.id()), argThat( arg -> arg.getInvoiceId().equals("INV NEW") && arg.getTotalDiscount() == null));
    }

    @Test 
    @WithMockUser(username = "admin", roles = "OWNER") 
    public void updateSupplyAPI_higherRole_shouldReturnUpdatedEntity() throws Exception{
        when(supplyService.updateSupply(any(), any(SupplyUpdateRequest.class))).thenReturn(supplyResponse);
        mockMvc.perform(
            patch("/api/supplies/" + supplyResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("invoiceId","INV NEW")  
            .param("totalFee","10")  
            
        )
        .andDo(print()) 
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.invoiceId").value(supplyResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty());

    }

    @Test 
    @WithMockUser(username = "admin", roles = "FINANCE") 
    public void updateSupplyAPI_invalidRole_shouldReturnUpdatedEntity() throws Exception{
        when(supplyService.updateSupply(any(), any(SupplyUpdateRequest.class))).thenReturn(supplyResponse);
        mockMvc.perform(
            patch("/api/supplies/" + supplyResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("invoiceId","INV NEW")  
            .param("totalFee","10")  
            
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority")); 
    
    }

    @Test
    @DisplayName("should return Supply Response when create succesfully")
    public void updateSupplyAPI_changeFeeBelow1_shouldReturnBadReq() throws Exception{ 
        mockMvc.perform(
            patch("/api/supplies/" + supplyResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("invoiceId","INV NEW")  
            .param("totalFee","0")  
            
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("totalFee cannot below 0")); 
    }

    @Test
    @DisplayName("should return Supply Response when create succesfully")
    public void updateSupplyAPI_notFoundSupply_shouldReturnNotFound() throws Exception{ 
        when(supplyService.updateSupply(any(), any(SupplyUpdateRequest.class))).thenThrow(new NotFoundEntityException("Supply with id is not found"));
        mockMvc.perform(
            patch("/api/supplies/" + supplyResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("invoiceId","INV NEW")  
            .param("totalFee","10")  
            
        )
        .andDo(print()) 
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Supply with id is not found")); 
    }

    @Test
    @DisplayName("should return Supply Response when create succesfully")
    public void updateSupplyAPI_supplyAlrCancelled_shouldReturnForbiddenReq() throws Exception{ 
        when(supplyService.updateSupply(any(), any(SupplyUpdateRequest.class))).thenThrow(new ForbiddenRequestException("Unable to cancel supply because Supply Status is CANCELLED"));

        mockMvc.perform(
            patch("/api/supplies/" + supplyResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("invoiceId","INV NEW")  
            .param("totalFee","10")  
            
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Unable to cancel supply because Supply Status is CANCELLED")); 
    }

    @Test
    @DisplayName("should return Supply Response when create succesfully")
    public void refundSupplyItemAPI_validRequest_shouldReturnUpdatedEntity() throws Exception{
        when(supplyService.refundSupplyItem(any(), any(ItemRefundRequest.class))).thenReturn(supplyResponse);
        mockMvc.perform(
            post("/api/supplies/" + supplyResponse.id() + "/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("productId", "1")  
            .param("quantity", "10")   
        )
        .andDo(print()) 
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.invoiceId").value(supplyResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty());

        verify(supplyService, times(1)).refundSupplyItem(eq(supplyResponse.id()), argThat( arg -> arg.getProductId() == 1L && arg.getQuantity() == 10L));
    }

    @Test 
    @WithMockUser(username = "admin", roles = "OWNER") 
    public void refundSupplyItemAPI_higherRole_shouldReturnUpdatedEntity() throws Exception{
        when(supplyService.refundSupplyItem(any(), any(ItemRefundRequest.class))).thenReturn(supplyResponse);
        mockMvc.perform(
            post("/api/supplies/" + supplyResponse.id() + "/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("productId", "1")  
            .param("quantity", "10")   
        )
        .andDo(print()) 
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.invoiceId").value(supplyResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty());

        verify(supplyService, times(1)).refundSupplyItem(eq(supplyResponse.id()), argThat( arg -> arg.getProductId() == 1L && arg.getQuantity() == 10L));
    }

    @Test 
    @WithMockUser(username = "admin", roles = "FINANCE") 
    public void refundSupplyItemAPI_invalidROle_shouldReturnUpdatedEntity() throws Exception{
        when(supplyService.refundSupplyItem(any(), any(ItemRefundRequest.class))).thenReturn(supplyResponse);
        mockMvc.perform(
            post("/api/supplies/" + supplyResponse.id() + "/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("productId", "1")  
            .param("quantity", "10")   
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority")); 
    }

    @Test
    @DisplayName("should return Supply Response when create succesfully")
    public void refundSupplyItemAPI_emptyProductId_shouldReturnUpdatedEntity() throws Exception{ 
        mockMvc.perform(
            post("/api/supplies/" + supplyResponse.id() + "/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("quantity", "10")   
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").value("productId cannot be empty"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("should return Supply Response when create succesfully")
    public void refundSupplyItemAPI_refundQuantityBelow1_shouldReturnUpdatedEntity() throws Exception{ 
        mockMvc.perform(
            post("/api/supplies/" + supplyResponse.id() + "/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("productId", "1")  
            .param("quantity", "0")   
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").value("quantity cannot below 1"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("should return Supply Response when create succesfully")
    public void refundSupplyItemAPI_notFound_shouldReturnUpdatedEntity() throws Exception{ 
        when(supplyService.refundSupplyItem(any(), any(ItemRefundRequest.class))).thenThrow(new NotFoundEntityException("Unable to find any supply items with product id "));

        mockMvc.perform(
            post("/api/supplies/" + supplyResponse.id() + "/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("productId", "1")  
            .param("quantity", "1")   
        )
        .andDo(print()) 
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors").value("Unable to find any supply items with product id "))
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("should return Supply Response when create succesfully")
    public void refundSupplyItemAPI_stockBelowThanRequestRefund_shouldReturnUpdatedEntity() throws Exception{ 
        when(supplyService.refundSupplyItem(any(), any(ItemRefundRequest.class))).thenThrow(new TransactionValidationException("Unable to cancel supply items, product with id doesn't have enough stock to refund, stock left: "));

        mockMvc.perform(
            post("/api/supplies/" + supplyResponse.id() + "/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("productId", "1")  
            .param("quantity", "1")   
        )
        .andDo(print()) 
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.errors").value("Unable to cancel supply items, product with id doesn't have enough stock to refund, stock left: "))
        .andExpect(jsonPath("$.data").isEmpty());
    }


    @Test
    @DisplayName("should return Supply Response when create succesfully")
    public void refundSupplyItemAPI_supplyAlrCancelled_shouldReturnUpdatedEntity() throws Exception{ 
        when(supplyService.refundSupplyItem(any(), any(ItemRefundRequest.class))).thenThrow(new ForbiddenRequestException("Unable to cancel supply because Supply Status is CANCELLED"));

        mockMvc.perform(
            post("/api/supplies/" + supplyResponse.id() + "/refund")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) 
            .param("productId", "1")  
            .param("quantity", "1")   
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").value("Unable to cancel supply because Supply Status is CANCELLED"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

}
