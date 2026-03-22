package lumi.insert.app.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
 
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.controller.wrapper.WebResponse;
import lumi.insert.app.dto.request.PaginationRequest;
import lumi.insert.app.dto.request.SupplyPaymentCreateRequest;
import lumi.insert.app.dto.request.SupplyPaymentGetByFilter;
import lumi.insert.app.dto.response.SupplyPaymentResponse; 
import lumi.insert.app.service.SupplyPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Transactional
@Slf4j
@Tag(name = "Supply Payments", description = "Endpoints for managing supply order payments and invoices")
public class SupplyPaymentController {
    
    @Autowired
    SupplyPaymentService supplyPaymentService;

    @Operation(summary = "Create supply payment", description = "Records a new payment made for a supply order")
    @ApiResponse(responseCode = "201", description = "Supply payment created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Supply order not found")
    @PostMapping(
        path = "/api/supplies/{supplyId}/payments",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @PreAuthorize("hasAnyRole('FINANCE')")
    ResponseEntity<WebResponse<SupplyPaymentResponse>> createSupplyPaymentAPI(@Parameter(description = "Supply order ID") @PathVariable(name = "supplyId") UUID supplyId, @ModelAttribute @Valid SupplyPaymentCreateRequest request){
        SupplyPaymentResponse resultFromService = supplyPaymentService.createSupplyPayment(supplyId, request);
        WebResponse<SupplyPaymentResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/" + resultFromService.id())
        .buildAndExpand(resultFromService.id())
        .toUri();

        return ResponseEntity.created(location).body(wrappedResult);
    }

    @Operation(summary = "Refund supply payment", description = "Records a refund for a payment in a supply order")
    @ApiResponse(responseCode = "201", description = "Supply payment refund created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Supply order not found")
    @PostMapping(
        path = "/api/supplies/{supplyId}/payments/refund",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @PreAuthorize("hasAnyRole('FINANCE')")
    ResponseEntity<WebResponse<SupplyPaymentResponse>> refundSupplyPaymentAPI(@Parameter(description = "Supply order ID") @PathVariable(name = "supplyId") UUID supplyId, @ModelAttribute @Valid @RequestBody SupplyPaymentCreateRequest request){
        SupplyPaymentResponse resultFromService = supplyPaymentService.refundSupplyPayment(supplyId, request);
        WebResponse<SupplyPaymentResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/" + resultFromService.id())
        .buildAndExpand(resultFromService.id())
        .toUri();

        return ResponseEntity.created(location).body(wrappedResult);
    }

    @Operation(summary = "Get supply payment by ID", description = "Retrieve detailed information about a specific supply payment")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved supply payment")
    @ApiResponse(responseCode = "404", description = "Supply payment not found")
    @GetMapping(
        path = "/api/supplies/{supplyId}/payments/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<SupplyPaymentResponse>> getSupplyPaymentAPI(@Parameter(description = "Supply order ID") @PathVariable(name = "supplyId") UUID supplyId, @Parameter(description = "Payment ID") @PathVariable(name = "id") UUID id){
        SupplyPaymentResponse resultFromService = supplyPaymentService.getSupplyPayment(id);

        WebResponse<SupplyPaymentResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Get supply payments", description = "Retrieve paginated list of all payments for a supply order")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved supply payments")
    @ApiResponse(responseCode = "404", description = "Supply order not found")
    @GetMapping(
        path = "/api/supplies/{supplyId}/payments",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<Slice<SupplyPaymentResponse>>> getSupplyPaymentsAPI(@Parameter(description = "Supply order ID") @PathVariable(name = "supplyId") UUID supplyId,@ModelAttribute @Valid PaginationRequest request){
        Slice<SupplyPaymentResponse> resultFromService = supplyPaymentService.getSupplyPaymentsBySupplyId(supplyId, request);

        WebResponse<Slice<SupplyPaymentResponse>> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Search supply payments", description = "Search supply payments with filtering options")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered supply payments")
    @ApiResponse(responseCode = "404", description = "Supply order not found")
    @GetMapping(
        path = "/api/supplies/{supplyId}/payments/search",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<Slice<SupplyPaymentResponse>>> searchSupplyPaymentsFilter(@Parameter(description = "Supply order ID") @PathVariable(name = "supplyId") UUID supplyId, @ModelAttribute @Valid SupplyPaymentGetByFilter request){
        log.info("{}", request);
        Slice<SupplyPaymentResponse> resultFromService = supplyPaymentService.getSupplyPaymentsByRequests(request);

        WebResponse<Slice<SupplyPaymentResponse>> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);
    }
}
