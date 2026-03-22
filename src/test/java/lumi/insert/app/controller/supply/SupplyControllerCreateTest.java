package lumi.insert.app.controller.supply;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map; 

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import com.github.f4b6a3.uuid.UuidCreator;

import lumi.insert.app.dto.request.SupplyCreateRequest;
import lumi.insert.app.dto.request.SupplyItemCreate; 
import lumi.insert.app.exception.NotFoundEntityException; 

public class SupplyControllerCreateTest extends BaseSupplyControllerTest{
    
    @Test
    @DisplayName("should return Supply Response when create succesfully")
    public void createSupplyAPI_validRequest_shouldReturnCreatedEntity() throws Exception{
        when(supplyService.createSupply(any(SupplyCreateRequest.class))).thenReturn(supplyResponse);
        List<SupplyItemCreate> items = List.of(new SupplyItemCreate(1L, 100L, 10L, null));

        SupplyCreateRequest request = SupplyCreateRequest.builder()
        .supplierId(UuidCreator.getTimeOrderedEpochFast())
        .invoiceId("INV")
        .totalFee(0L)
        .totalDiscount(0L)
        .supplyItems(items)
        .build();

        mockMvc.perform(
            post("/api/supplies")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request))
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.invoiceId").value(supplyResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty());

        verify(supplyService, times(1)).createSupply(argThat( arg -> arg.getSupplyItems().size() == 1 && arg.getSupplyItems().getFirst().getProductId() == 1L));
    }

    @Test
    @WithMockUser(username = "admin", roles = "OWNER") 
    public void createSupplyAPI_higherRole_shouldReturnCreatedEntity() throws Exception{
        when(supplyService.createSupply(any(SupplyCreateRequest.class))).thenReturn(supplyResponse);
        List<SupplyItemCreate> items = List.of(new SupplyItemCreate(1L, 100L, 10L, null));

        SupplyCreateRequest request = SupplyCreateRequest.builder()
        .supplierId(UuidCreator.getTimeOrderedEpochFast())
        .invoiceId("INV")
        .totalFee(0L)
        .totalDiscount(0L)
        .supplyItems(items)
        .build();

        mockMvc.perform(
            post("/api/supplies")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request))
        )
        .andDo(print()) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.invoiceId").value(supplyResponse.invoiceId()))
        .andExpect(jsonPath("$.errors").isEmpty());

      }

    @Test
    @WithMockUser(username = "admin", roles = "FINANCE") 
    public void createSupplyAPI_invalidRole_shouldReturnCreatedEntity() throws Exception{
        when(supplyService.createSupply(any(SupplyCreateRequest.class))).thenReturn(supplyResponse);
        List<SupplyItemCreate> items = List.of(new SupplyItemCreate(1L, 100L, 10L, null));

        SupplyCreateRequest request = SupplyCreateRequest.builder()
        .supplierId(UuidCreator.getTimeOrderedEpochFast())
        .invoiceId("INV")
        .totalFee(0L)
        .totalDiscount(0L)
        .supplyItems(items)
        .build();

        mockMvc.perform(
            post("/api/supplies")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request))
        )
        .andDo(print()) 
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Access denied, require an authority"));  

      }

    @Test
    @DisplayName("should return errors NotNull when request param is not valid")
    public void createSupplyAPI_nullCustomerId_shouldReturnNotNullError() throws Exception{ 
        List<SupplyItemCreate> items = List.of(new SupplyItemCreate(1L, 100L, 10L, null));

        SupplyCreateRequest request = SupplyCreateRequest.builder()
        .supplierId(UuidCreator.getTimeOrderedEpochFast())
        .invoiceId("INV")
        .totalFee(-10L)
        .totalDiscount(0L)
        .supplyItems(items)
        .build();

        mockMvc.perform(
            post("/api/supplies")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request))
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").value("totalFee cannot below 0"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("should return errors NotNull when request param is not valid")
    public void createSupplyAPI_emptyHeaderParam_shouldReturnNotNullError() throws Exception{ 
        List<SupplyItemCreate> items = List.of(new SupplyItemCreate(1L, 100L, 10L, null));

        SupplyCreateRequest request = SupplyCreateRequest.builder()
        .supplierId(UuidCreator.getTimeOrderedEpochFast())
        .invoiceId("INV") 
        .totalDiscount(0L)
        .supplyItems(items)
        .build();

        mockMvc.perform(
            post("/api/supplies")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request))
        ) 
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").value("totalFee cannot be empty"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("should return errors NotNull when request param is not valid")
    public void createSupplyAPI_emptyWhiteSpace_shouldReturnNotNullError() throws Exception{
        when(supplyService.createSupply(any(SupplyCreateRequest.class))).thenReturn(supplyResponse);

        Map<String, String> request = new HashMap<>();
        request.put("supplierId", " ");
 
        mockMvc.perform(
            post("/api/supplies")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE) 
            .content(objectMapper.writeValueAsString(request)) 
        )
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").isNotEmpty())
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("should return errors NotNull when request param is not valid")
    public void createSupplyAPI_nullItems_shouldReturnNotNullError() throws Exception{ 
 
        SupplyCreateRequest request = SupplyCreateRequest.builder()
        .supplierId(UuidCreator.getTimeOrderedEpochFast())
        .invoiceId("INV")
        .totalFee(0L)
        .totalDiscount(0L) 
        .build();

        mockMvc.perform(
            post("/api/supplies")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request))
        ) 
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").value("items cannot be empty"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("should return Supply Response when create succesfully")
    public void createSupplyAPI_invalidParamOfItems_shouldReturnCreatedEntity() throws Exception{
        when(supplyService.createSupply(any(SupplyCreateRequest.class))).thenReturn(supplyResponse);

        List<SupplyItemCreate> items = List.of(new SupplyItemCreate(1L, 100L, null, null));

        SupplyCreateRequest request = SupplyCreateRequest.builder()
        .supplierId(UuidCreator.getTimeOrderedEpochFast())
        .invoiceId("INV")
        .totalFee(0L)
        .totalDiscount(0L)
        .supplyItems(items)
        .build();

        mockMvc.perform(
            post("/api/supplies")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request))
        ) 
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").value("quantity cannot be empty"))
        .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("should return Supply Response when create succesfully")
    public void createSupplyAPI_minusProductQuantityAtItems_shouldReturnCreatedEntity() throws Exception{
        when(supplyService.createSupply(any(SupplyCreateRequest.class))).thenReturn(supplyResponse);
        List<SupplyItemCreate> items = List.of(new SupplyItemCreate(1L, 100L, -10L, null));

        SupplyCreateRequest request = SupplyCreateRequest.builder()
        .supplierId(UuidCreator.getTimeOrderedEpochFast())
        .invoiceId("INV")
        .totalFee(0L)
        .totalDiscount(0L)
        .supplyItems(items)
        .build();

        mockMvc.perform(
            post("/api/supplies")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request))
        ) 
        .andDo(print()) 
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").value("quantity cannot below 0"))
        .andExpect(jsonPath("$.data").isEmpty());
    } 

    @Test
    @DisplayName("should return errors of notFoundEntity when request Trx id isn't valid")
    public void createSupplyAPI_invalidSupplierId_shouldReturnErrors() throws Exception{
        when(supplyService.createSupply(any(SupplyCreateRequest.class))).thenThrow(new NotFoundEntityException("Supplier with id is not found"));

        List<SupplyItemCreate> items = List.of(new SupplyItemCreate(1L, 100L, 10L, null));

        SupplyCreateRequest request = SupplyCreateRequest.builder()
        .supplierId(UuidCreator.getTimeOrderedEpochFast())
        .invoiceId("INV")
        .totalFee(0L)
        .totalDiscount(0L)
        .supplyItems(items)
        .build();

        mockMvc.perform(
            post("/api/supplies")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request))
        )
        .andDo(print()) 
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Supplier with id is not found")); 
    }
}
