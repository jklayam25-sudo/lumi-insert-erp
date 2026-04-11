package lumi.insert.app.controller;
 
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.controller.wrapper.WebResponse;
import lumi.insert.app.dto.request.ItemRefundRequest;
import lumi.insert.app.dto.request.SupplyCreateRequest;
import lumi.insert.app.dto.request.SupplyGetByFilter; 
import lumi.insert.app.dto.request.SupplyUpdateRequest; 
import lumi.insert.app.dto.response.SupplyDetailResponse;
import lumi.insert.app.dto.response.SupplyResponse; 
import lumi.insert.app.service.PdfService;
import lumi.insert.app.service.SupplyService;
import lumi.insert.app.service.XlsxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Slf4j
@Tag(name = "Supplies", description = "Endpoints for managing purchase supplies and inventory receiving")
public class SupplyController {
    
    @Autowired
    SupplyService supplyService;

    @Autowired
    PdfService pdfService;

    @Autowired
    XlsxService xlsxService;

    @Operation(summary = "Create new supply order", description = "Creates a new purchase supply order with specified items")
    @ApiResponse(responseCode = "201", description = "Supply order created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping(
        path = "/api/supplies",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('WAREHOUSE')")
    ResponseEntity<WebResponse<SupplyResponse>> createSupply(@Valid @RequestBody @org.springframework.web.bind.annotation.RequestBody SupplyCreateRequest request){
        log.debug("Supply creation request: {}", request);
        log.info("Request for new supply order from supplier with ID {}", request.getSupplierId());
        
        SupplyResponse resultFromService = supplyService.createSupply(request);
        log.debug("Supply created: {}", resultFromService);
 
        WebResponse<SupplyResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/" + resultFromService.id())
        .buildAndExpand(resultFromService.id())
        .toUri();

        log.info("Supply order created successfully with ID: {}", resultFromService.getId());
        return ResponseEntity.created(location).body(wrappedResult);   
    }

    @Operation(summary = "Get supply order by ID", description = "Retrieve detailed information about a specific supply order")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved supply order")
    @ApiResponse(responseCode = "404", description = "Supply order not found")
    @GetMapping(
        path = "/api/supplies/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<SupplyDetailResponse>> getSupply(@Parameter(description = "Supply order ID") @PathVariable(name = "id") UUID id){
        log.debug("Supply search by id request: {}", id);
        SupplyDetailResponse resultFromService = supplyService.getSupply(id);
        log.debug("Supply found: {}", resultFromService);
        
        WebResponse<SupplyDetailResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Export supply order to PDF", description = "Generates a PDF document of the supply order with all items")
    @ApiResponse(responseCode = "200", description = "Successfully exported supply order to PDF")
    @ApiResponse(responseCode = "404", description = "Supply order not found")
    @GetMapping(
        path = "/api/supplies/{id}/pdf",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    ResponseEntity<InputStreamResource> exportSupply(@Parameter(description = "Supply order ID") @PathVariable(name = "id") UUID id){
        log.info("Supply PDF export request for ID: {}", id);
        SupplyDetailResponse resultFromService = supplyService.getSupply(id);
        log.debug("Supply detail for export: {}. Converting data to Pdf...", resultFromService);
        ByteArrayInputStream pdf = pdfService.exportSupplyWithItems(resultFromService);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename="+ resultFromService.invoiceId() + ".pdf");

        log.info("Supply PDF export completed for ID: {}", id);
        return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new InputStreamResource(pdf));
    }

    @Operation(summary = "Search supply orders", description = "Retrieve paginated list of supply orders with optional filtering")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved supply orders")
    @GetMapping(
        path = "/api/supplies/search",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<Slice<SupplyResponse>>> getSupplies(@ModelAttribute @Valid SupplyGetByFilter request){
        log.debug("Supplies search request: {}", request);
        Slice<SupplyResponse> resultFromService = supplyService.searchSuppliesByRequests(request);
        log.debug("Supplies found: {}", resultFromService);
        
        WebResponse<Slice<SupplyResponse>> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);
    } 

    @Operation(summary = "Export supplies using filters to XLSX", description = "Export specific supplies with filtering options")
    @ApiResponse(responseCode = "200", description = "Successfully exported supply list to XLSX")
    @GetMapping(
        path = "/api/supplies/history/export",
        produces = MediaType.APPLICATION_XML_VALUE
    )
    void exportSupplies(@ModelAttribute @Valid SupplyGetByFilter request, HttpServletResponse response) throws IOException{
        log.debug("Supply history export request with filter: {}", request);
        log.debug("Supply history export request with filter");
        request.setSize(99999000);
        Slice<SupplyResponse> resultFromService = supplyService.searchSuppliesByRequests(request);
        log.debug("Supplies for export: {}. Converting data to Xlsx...", resultFromService);
        
        response.setContentType("application/xml");
        response.addHeader("Content-Disposition", "attachment; filename=supplyHistory" + ".xlsx");
        xlsxService.exportSupplies(resultFromService.getContent(), response.getOutputStream());
        log.info("Supply history export completed successfully");
    } 
 
    @Operation(summary = "Cancel supply order", description = "Cancels an existing supply order")
    @ApiResponse(responseCode = "200", description = "Supply order cancelled successfully")
    @ApiResponse(responseCode = "404", description = "Supply order not found")
    @PostMapping(
        path = "/api/supplies/{id}/cancel",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('WAREHOUSE')")
    ResponseEntity<WebResponse<SupplyResponse>> cancelSupply(@Parameter(description = "Supply order ID") @PathVariable(name = "id") UUID id){
        log.info("Cancel supply request for ID: {}", id);
        SupplyResponse resultFromService = supplyService.cancelSupply(id);
        log.debug("Supply cancelled: {}", resultFromService);
 
        WebResponse<SupplyResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);
 
        log.info("Supply cancelled successfully for ID: {}", id);
        return ResponseEntity.ok(wrappedResult);   
    } 

    @Operation(summary = "Update supply order", description = "Updates information for an existing supply order")
    @ApiResponse(responseCode = "200", description = "Supply order updated successfully")
    @ApiResponse(responseCode = "404", description = "Supply order not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PatchMapping(
        path = "/api/supplies/{id}",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('WAREHOUSE')")
    ResponseEntity<WebResponse<SupplyResponse>> updateSupplierAPI(@Parameter(description = "Supply order ID") @PathVariable(name = "id") UUID id, @Valid @RequestBody SupplyUpdateRequest request){ 
        log.info("Update supply request for ID: {}", id);
        log.debug("Supply ID: {}. Update request: {}", id, request);
        
        SupplyResponse resultFromService = supplyService.updateSupply(id, request);
        log.debug("Supply updated: {}", resultFromService);

        WebResponse<SupplyResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);
        
        log.info("Successfully updated supply with ID: {}", id);
        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Refund supply item", description = "Records a refund for a specific item in the supply order")
    @ApiResponse(responseCode = "200", description = "Supply item refunded successfully")
    @ApiResponse(responseCode = "404", description = "Supply order not found")
    @ApiResponse(responseCode = "400", description = "Invalid refund request")
    @PostMapping(
        path = "/api/supplies/{id}/refund",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('WAREHOUSE')")
    ResponseEntity<WebResponse<SupplyResponse>> refundSupplyItem(@Parameter(description = "Supply order ID") @PathVariable(name = "id") UUID id, @Valid ItemRefundRequest request){
        log.info("Refund supply item request for supply ID: {}", id);
        log.debug("Refund request: {}", request);
        
        SupplyResponse resultFromService = supplyService.refundSupplyItem(id, request);
        log.debug("Supply item refunded: {}", resultFromService);
 
        WebResponse<SupplyResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);
        
        log.info("Supply item refunded successfully for supply ID: {}", id);
        return ResponseEntity.ok(wrappedResult);   
    } 
}
