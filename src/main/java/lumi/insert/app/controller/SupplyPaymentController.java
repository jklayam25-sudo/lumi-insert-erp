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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
 
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.controller.wrapper.WebResponse;
import lumi.insert.app.dto.request.PaginationRequest;
import lumi.insert.app.dto.request.SupplyPaymentCreateRequest;
import lumi.insert.app.dto.request.SupplyPaymentGetByFilter;
import lumi.insert.app.dto.response.SupplyPaymentResponse;
import lumi.insert.app.exception.ForbiddenRequestException; 
import lumi.insert.app.service.SupplyPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller to access {@link SupplyPaymentService}.
 * Endpoints for managing supply order payments and invoices.
 * @author KelvinKhodes
 * @since 1.0.0
 */
@RestController
@Transactional
@Slf4j
@Tag(name = "Supply Payments", description = "Endpoints for managing supply order payments and invoices")
public class SupplyPaymentController {
    
    @Autowired
    SupplyPaymentService supplyPaymentService;

    private final int fileUploadSize = 8 * 1024 * 1024;

/**
 * Records a new payment made for a supply order
 */
    @Operation(summary = "Create supply payment", description = "Records a new payment made for a supply order")
    @ApiResponse(responseCode = "201", description = "Supply payment created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Supply order not found")
    @PostMapping(
        path = "/api/supplies/{supplyId}/payments",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('FINANCE')")
    ResponseEntity<WebResponse<SupplyPaymentResponse>> createSupplyPaymentAPI(@Parameter(description = "Supply order ID") @PathVariable(name = "supplyId") UUID supplyId, @ModelAttribute @Valid SupplyPaymentCreateRequest request){
        log.info("Create supply payment request for supply ID: {}", supplyId);
        log.debug("Payment creation request: {}", request);
        
        for (MultipartFile file : request.getFiles()) {
            if(file.isEmpty()) throw new ForbiddenRequestException("File picture cannot be empty!");
            if(file.getSize() > fileUploadSize) throw new ForbiddenRequestException("File size must be less than 8Mb");
            if(!(file.getContentType().contains("image"))) throw new ForbiddenRequestException("File format type must be image");  
        }
        
        SupplyPaymentResponse resultFromService = supplyPaymentService.createSupplyPayment(supplyId, request);
        log.debug("Supply payment created: {}", resultFromService);
        WebResponse<SupplyPaymentResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/" + resultFromService.id())
        .buildAndExpand(resultFromService.id())
        .toUri();

        log.info("Supply payment created successfully with ID: {}", resultFromService.getId());
        return ResponseEntity.created(location).body(wrappedResult);
    }

/**
 * Records a refund for a payment in a supply order
 */
    @Operation(summary = "Refund supply payment", description = "Records a refund for a payment in a supply order")
    @ApiResponse(responseCode = "201", description = "Supply payment refund created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Supply order not found")
    @PostMapping(
        path = "/api/supplies/{supplyId}/payments/refund",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('FINANCE')")
    ResponseEntity<WebResponse<SupplyPaymentResponse>> refundSupplyPaymentAPI(@Parameter(description = "Supply order ID") @PathVariable(name = "supplyId") UUID supplyId, @ModelAttribute @Valid @RequestBody SupplyPaymentCreateRequest request){
        log.info("Refund supply payment request for supply ID: {}", supplyId);
        log.debug("Payment refund request: {}", request);
        
        for (MultipartFile file : request.getFiles()) {
            if(file.isEmpty()) throw new ForbiddenRequestException("File picture cannot be empty!");
            if(file.getSize() > fileUploadSize) throw new ForbiddenRequestException("File size must be less than 8Mb");
            if(!(file.getContentType().contains("image"))) throw new ForbiddenRequestException("File format type must be image");  
        }
        
        SupplyPaymentResponse resultFromService = supplyPaymentService.refundSupplyPayment(supplyId, request);
        log.debug("Supply payment refunded: {}", resultFromService);
        WebResponse<SupplyPaymentResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/" + resultFromService.id())
        .buildAndExpand(resultFromService.id())
        .toUri();

        log.info("Supply payment refund created successfully with ID: {}", resultFromService.getId());
        return ResponseEntity.created(location).body(wrappedResult);
    }

/**
 * Retrieve detailed information about a specific supply payment
 */
    @Operation(summary = "Get supply payment by ID", description = "Retrieve detailed information about a specific supply payment")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved supply payment")
    @ApiResponse(responseCode = "404", description = "Supply payment not found")
    @GetMapping(
        path = "/api/supplies/{supplyId}/payments/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<SupplyPaymentResponse>> getSupplyPaymentAPI(@Parameter(description = "Supply order ID") @PathVariable(name = "supplyId") UUID supplyId, @Parameter(description = "Payment ID") @PathVariable(name = "id") UUID id){
        log.debug("Supply payment search by id request: {}", id);
        SupplyPaymentResponse resultFromService = supplyPaymentService.getSupplyPayment(id);
        log.debug("Supply payment found: {}", resultFromService);

        WebResponse<SupplyPaymentResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);
    }

/**
 * Retrieve paginated list of all payments for a supply order
 */
    @Operation(summary = "Get supply payments", description = "Retrieve paginated list of all payments for a supply order")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved supply payments")
    @ApiResponse(responseCode = "404", description = "Supply order not found")
    @GetMapping(
        path = "/api/supplies/{supplyId}/payments",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<Slice<SupplyPaymentResponse>>> getSupplyPaymentsAPI(@Parameter(description = "Supply order ID") @PathVariable(name = "supplyId") UUID supplyId,@ModelAttribute @Valid PaginationRequest request){
        log.debug("Supply payments list request for supply ID: {}, pagination: {}", supplyId, request);
        Slice<SupplyPaymentResponse> resultFromService = supplyPaymentService.getSupplyPaymentsBySupplyId(supplyId, request);
        log.debug("Supply payments found: {}", resultFromService);

        WebResponse<Slice<SupplyPaymentResponse>> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);
    }

/**
 * Search supply payments with filtering options
 */
    @Operation(summary = "Search supply payments", description = "Search supply payments with filtering options")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered supply payments")
    @ApiResponse(responseCode = "404", description = "Supply order not found")
    @GetMapping(
        path = "/api/supplies/{supplyId}/payments/search",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<Slice<SupplyPaymentResponse>>> searchSupplyPaymentsFilter(@Parameter(description = "Supply order ID") @PathVariable(name = "supplyId") UUID supplyId, @ModelAttribute @Valid SupplyPaymentGetByFilter request){
        log.debug("Supply payments search request with filter: {}", request);
        Slice<SupplyPaymentResponse> resultFromService = supplyPaymentService.getSupplyPaymentsByRequests(request);
        log.debug("Supply payments found: {}", resultFromService);

        WebResponse<Slice<SupplyPaymentResponse>> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);
    }
}
