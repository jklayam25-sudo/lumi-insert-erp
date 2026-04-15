package lumi.insert.app.controller.supplypayment;
 
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
 
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;

import lumi.insert.app.dto.request.PaginationRequest;
import lumi.insert.app.dto.request.SupplyPaymentGetByFilter;
import lumi.insert.app.dto.response.SupplyPaymentResponse; 
import lumi.insert.app.exception.NotFoundEntityException;

public class SupplyPaymentControllerGetTest extends BaseSupplyPaymentControllerTest{
    
    @Test
    @DisplayName("Should return supplyPayment DTO when found")
    void getSupplyPaymentAPI_validRequest_shouldReturnEntityDTO() throws Exception{
        when(supplyPaymentService.getSupplyPayment(supplyPaymentResponse.id())).thenReturn(supplyPaymentResponse);

        mockMvc.perform(
            get("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments/" + supplyPaymentResponse.id().toString())
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors").isEmpty())
        .andExpect(jsonPath("$.data.id").value(supplyPaymentResponse.id().toString()));
    }

    @Test
    @DisplayName("Should return error missmatch type when request id is not UUID")
    void getSupplyPaymentAPI_missMatch_shouldReturnErrorMissMatch() throws Exception{
        when(supplyPaymentService.getSupplyPayment(supplyPaymentResponse.id())).thenReturn(supplyPaymentResponse);

        mockMvc.perform(
            get("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments/" + true)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").isNotEmpty()); 
    }

    @Test
    @DisplayName("Should return error NotFound when request supply payment not found")
    void getSupplyPaymentAPI_notFound_shouldReturnErrorNotFound() throws Exception{
        when(supplyPaymentService.getSupplyPayment(supplyPaymentResponse.id())).thenThrow(new NotFoundEntityException("Supply Payment with ID " + 1L + " was not found"));

        mockMvc.perform(
            get("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments/" + supplyPaymentResponse.id())
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.errors").value("Supply Payment with ID " + 1L + " was not found")); 
    }

    @Test
    @DisplayName("Should return slice of supplyPayment DTO when supply found/valid")
    void getSupplyPaymentsAPI_validRequest_shouldReturnSliceEntityDTO() throws Exception{
        Slice<SupplyPaymentResponse> slice = new SliceImpl<SupplyPaymentResponse>(List.of(supplyPaymentResponse));
        when(supplyPaymentService.getSupplyPaymentsBySupplyId(supplyPaymentResponse.supplyId(), PaginationRequest.builder().build())).thenReturn(slice);

        mockMvc.perform(
            get("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors").isEmpty())
        .andExpect(jsonPath("$.data.content[0].id").value(supplyPaymentResponse.id().toString()))
        .andExpect(jsonPath("$.data.content.length()").value(slice.getNumberOfElements()));
        verify(supplyPaymentService, times(1)).getSupplyPaymentsBySupplyId(supplyPaymentResponse.supplyId(), PaginationRequest.builder().build());
    }

    @Test
    @DisplayName("Should return slice with 0 data when supply found/valid but no payment")
    void getSupplyPaymentsAPI_validRequestCaseNoPayment_shouldReturnSliceEntityDTO() throws Exception{
        Slice<SupplyPaymentResponse> slice = new SliceImpl<SupplyPaymentResponse>(List.of());
        when(supplyPaymentService.getSupplyPaymentsBySupplyId(supplyPaymentResponse.supplyId(), PaginationRequest.builder().build())).thenReturn(slice);

        mockMvc.perform(
            get("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments")
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors").isEmpty())
        .andExpect(jsonPath("$.data.content").isArray())
        .andExpect(jsonPath("$.data.content.length()").value(slice.getNumberOfElements()));
        verify(supplyPaymentService, times(1)).getSupplyPaymentsBySupplyId(supplyPaymentResponse.supplyId(), PaginationRequest.builder().build());
    }

    @Test
    @DisplayName("Should return slice of supplyPayment DTO when supply found/valid")
    void searchSupplyPaymentsFilterAPI_validRequest_shouldReturnSliceEntityDTO() throws Exception{
        Slice<SupplyPaymentResponse> slice = new SliceImpl<SupplyPaymentResponse>(List.of(supplyPaymentResponse));
        when(supplyPaymentService.getSupplyPaymentsByRequests(SupplyPaymentGetByFilter.builder().supplyId(supplyPaymentResponse.supplyId()).minTotalPayment(BigDecimal.valueOf(5000L)).build())).thenReturn(slice);

        mockMvc.perform(
            get("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments/search?minTotalPayment=5000")
            .accept(MediaType.APPLICATION_JSON_VALUE) 
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors").isEmpty())
        .andExpect(jsonPath("$.data.content[0].id").value(supplyPaymentResponse.id().toString()))
        .andExpect(jsonPath("$.data.content.length()").value(slice.getNumberOfElements())); 
    }

    @Test
    @DisplayName("Should return error missmatch when request param type missmatch")
    void searchSupplyPaymentsFilterAPI_missmatchParam_shouldReturnSliceEntityDTO() throws Exception{
        Slice<SupplyPaymentResponse> slice = new SliceImpl<SupplyPaymentResponse>(List.of(supplyPaymentResponse));
        when(supplyPaymentService.getSupplyPaymentsByRequests(SupplyPaymentGetByFilter.builder().supplyId(supplyPaymentResponse.supplyId()).minTotalPayment(BigDecimal.valueOf(5000L)).build())).thenReturn(slice);

        mockMvc.perform(
            get("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments/search?minTotalPayment=true")
            .accept(MediaType.APPLICATION_JSON_VALUE) 
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("Should return error badrequest  when param filter totalpayment is lower than 0")
    void searchSupplyPaymentsFilterAPI_minTotalPaymentMinus_shouldReturnSliceEntityDTO() throws Exception{
        Slice<SupplyPaymentResponse> slice = new SliceImpl<SupplyPaymentResponse>(List.of(supplyPaymentResponse));
        when(supplyPaymentService.getSupplyPaymentsByRequests(SupplyPaymentGetByFilter.builder().supplyId(supplyPaymentResponse.supplyId()).minTotalPayment(BigDecimal.valueOf(5000L)).build())).thenReturn(slice);

        mockMvc.perform(
            get("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments/search?minTotalPayment=-5000")
            .accept(MediaType.APPLICATION_JSON_VALUE) 
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").value("minTotalPayment minimal value is 0"));
    }

    @Test
    @DisplayName("Should return error badrequest  when param filter totalpayment is lower than 0")
    void searchSupplyPaymentsFilterAPI_wrongSortByValue_shouldReturnSliceEntityDTO() throws Exception{
        Slice<SupplyPaymentResponse> slice = new SliceImpl<SupplyPaymentResponse>(List.of(supplyPaymentResponse));
        when(supplyPaymentService.getSupplyPaymentsByRequests(SupplyPaymentGetByFilter.builder().supplyId(supplyPaymentResponse.supplyId()).minTotalPayment(BigDecimal.valueOf(5000L)).build())).thenReturn(slice);

        mockMvc.perform(
            get("/api/supplies/" + supplyPaymentResponse.supplyId() + "/payments/search?minTotalPayment=5000&sortBy=cc")
            .accept(MediaType.APPLICATION_JSON_VALUE) 
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").value("check documentation for sortBy specification"));
    }
}
