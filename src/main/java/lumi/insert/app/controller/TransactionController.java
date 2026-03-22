package lumi.insert.app.controller;
 
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI; 
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.controller.wrapper.WebResponse; 
import lumi.insert.app.dto.request.TransactionCreateRequest;
import lumi.insert.app.dto.request.TransactionGetByFilter;

import lumi.insert.app.dto.response.TransactionDetailResponse;
import lumi.insert.app.dto.response.TransactionResponse;
import lumi.insert.app.service.PdfService;
import lumi.insert.app.service.TransactionService;
import lumi.insert.app.service.XlsxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Slf4j
@Tag(name = "Transactions", description = "Endpoints for managing sales transactions and inventory operations")
public class TransactionController {
    
    @Autowired
    TransactionService transactionService;

    @Autowired
    PdfService pdfService;

    @Autowired
    XlsxService xlsxService;

    @Operation(summary = "Create new transaction", description = "Creates a new sales transaction with specified items and customer")
    @ApiResponse(responseCode = "201", description = "Transaction created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping(
        path = "/api/transactions",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @PreAuthorize("hasAnyRole('CASHIER')")
    ResponseEntity<WebResponse<TransactionResponse>> createTransaction(@Valid @RequestBody TransactionCreateRequest request){
        TransactionResponse resultFromService = transactionService.createTransaction(request);
 
        WebResponse<TransactionResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/" + resultFromService.id())
        .buildAndExpand(resultFromService.id())
        .toUri();

        return ResponseEntity.created(location).body(wrappedResult);   
    }

    @Operation(summary = "Get transaction by ID", description = "Retrieve detailed information about a specific transaction")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @GetMapping(
        path = "/api/transactions/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<TransactionResponse>> getTransaction(@Parameter(description = "Transaction ID") @PathVariable(name = "id") UUID id){
        TransactionResponse resultFromService = transactionService.getTransaction(id);
        
        WebResponse<TransactionResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Get transactions with filters", description = "Retrieve paginated list of transactions with filtering options")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions")
    @GetMapping(
        path = "/api/transactions/filter",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<Slice<TransactionResponse>>> getTransactions(@ModelAttribute @Valid TransactionGetByFilter request){
        Slice<TransactionResponse> resultFromService = transactionService.searchTransactionsByRequests(request);
        
        WebResponse<Slice<TransactionResponse>> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Export transactions using filters to XLSX", description = "Export specific transactions with filtering options")
    @ApiResponse(responseCode = "200", description = "Successfully exported transactions list to XLSX")
    @GetMapping(
        path = "/api/transactions/history/export",
        produces = MediaType.APPLICATION_XML_VALUE
    )
    void exportTransactionsHistory(@ModelAttribute @Valid TransactionGetByFilter request, HttpServletResponse response) throws IOException{
        request.setSize(99999000);
        Slice<TransactionResponse> resultFromService = transactionService.searchTransactionsByRequests(request);

        response.setContentType("application/xml");
        response.addHeader("Content-Disposition", "attachment; filename=transactionHistory" + ".xlsx");
        xlsxService.exportTransactions(resultFromService.getContent(), response.getOutputStream());
    }

    @Operation(summary = "Export transaction order to PDF", description = "Generates a PDF document of the transaction order with all items")
    @ApiResponse(responseCode = "200", description = "Successfully exported transaction order to PDF")
    @ApiResponse(responseCode = "404", description = "Supply order not found")
    @GetMapping(
        path = "/api/transactions/{id}/pdf",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    ResponseEntity<InputStreamResource> exportTransaction(@Parameter(description = "Transaction ID") @PathVariable(name = "id") UUID id){
        TransactionDetailResponse resultFromService = transactionService.getTransactionDetail(id);
        ByteArrayInputStream pdf = pdfService.exportTransactionWithItems(resultFromService);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename="+ resultFromService.invoiceId() + ".pdf");

        return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new InputStreamResource(pdf));
    }

    @Operation(summary = "Process transaction", description = "Marks a transaction as processed and updates inventory accordingly")
    @ApiResponse(responseCode = "200", description = "Transaction processed successfully")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @PostMapping(
        path = "/api/transactions/{id}/process",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CASHIER')")
    ResponseEntity<WebResponse<TransactionResponse>> processTransaction(@Parameter(description = "Transaction ID") @PathVariable(name = "id") UUID id){
        TransactionResponse resultFromService = transactionService.setTransactionToProcess(id);
 
        WebResponse<TransactionResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);
 
        return ResponseEntity.ok(wrappedResult);   
    }

    @Operation(summary = "Cancel transaction", description = "Cancels a transaction and reverses inventory changes")
    @ApiResponse(responseCode = "200", description = "Transaction cancelled successfully")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @PostMapping(
        path = "/api/transactions/{id}/cancel",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CASHIER')")
    ResponseEntity<WebResponse<TransactionResponse>> cancelTransaction(@Parameter(description = "Transaction ID") @PathVariable(name = "id") UUID id){
        TransactionResponse resultFromService = transactionService.cancelTransaction(id);
 
        WebResponse<TransactionResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);
 
        return ResponseEntity.ok(wrappedResult);   
    }

    @Operation(summary = "Refresh transaction", description = "Recalculates transaction totals and balances")
    @ApiResponse(responseCode = "200", description = "Transaction refreshed successfully")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @PostMapping(
        path = "/api/transactions/{id}/refresh",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CASHIER')")
    ResponseEntity<WebResponse<TransactionResponse>> refreshTransaction(@Parameter(description = "Transaction ID") @PathVariable(name = "id") UUID id){
        TransactionResponse resultFromService = transactionService.refreshTransaction(id);
 
        WebResponse<TransactionResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);
 
        return ResponseEntity.ok(wrappedResult);   
    }
}
