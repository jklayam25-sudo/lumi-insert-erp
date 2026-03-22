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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lumi.insert.app.controller.wrapper.WebResponse;
import lumi.insert.app.core.entity.nondatabase.SliceIndex;
import lumi.insert.app.dto.request.CustomerCreateRequest;
import lumi.insert.app.dto.request.CustomerGetByFilter;
import lumi.insert.app.dto.request.CustomerGetNameRequest;
import lumi.insert.app.dto.request.CustomerUpdateRequest; 
import lumi.insert.app.dto.response.CustomerDetailResponse;
import lumi.insert.app.dto.response.CustomerNameResponse;
import lumi.insert.app.dto.response.CustomerResponse;
import lumi.insert.app.service.CustomerService;

@RestController
@Slf4j
@Tag(name = "Customers", description = "Endpoints for managing customer information and details")
public class CustomerController {
    
    @Autowired
    CustomerService customerService;

    @Operation(summary = "Create new customer", description = "Creates a new customer with the specified details including location")
    @ApiResponse(responseCode = "201", description = "Customer created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping(
        path = "/api/customers",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @PreAuthorize("hasAnyRole('CASHIER', 'FINANCE')")
    ResponseEntity<WebResponse<CustomerDetailResponse>> createCustomerAPI(@Valid @RequestBody CustomerCreateRequest request){
        
        CustomerDetailResponse resultFromService = customerService.createCustomer(request);

        WebResponse<CustomerDetailResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/" + resultFromService.id())
        .buildAndExpand(resultFromService.id())
        .toUri();

        return ResponseEntity.created(location).body(wrappedResult);
    }

    @Operation(summary = "Get customer by ID", description = "Retrieve detailed information about a specific customer")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @GetMapping(
        path = "/api/customers/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<CustomerDetailResponse>> getCustomerAPI(@Parameter(description = "Customer ID") @PathVariable(name = "id") UUID id){
        CustomerDetailResponse resultFromService = customerService.getCustomer(id);

        WebResponse<CustomerDetailResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Get all customers", description = "Retrieve paginated list of customers with optional filtering")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customers")
    @GetMapping(
        path = "/api/customers",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<Slice<CustomerResponse>>> getCustomersAPI(@Valid @ModelAttribute CustomerGetByFilter request){ 
        Slice<CustomerResponse> resultFromService = customerService.getCustomers(request);

        WebResponse<Slice<CustomerResponse>> wrappedResult = WebResponse.getWrapper(resultFromService, null);
 
        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Search customer names", description = "Search for customers by name with pagination support")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved matching customers")
    @GetMapping(
        path = "/api/customers/searchName",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<SliceIndex<CustomerNameResponse>>> searchCustomerNamesAPI(@Valid @ModelAttribute CustomerGetNameRequest request){
        SliceIndex<CustomerNameResponse> resultFromService = customerService.searchCustomerNames(request);

        WebResponse<SliceIndex<CustomerNameResponse>> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);   
    }

    @Operation(summary = "Update customer", description = "Updates information for an existing customer")
    @ApiResponse(responseCode = "200", description = "Customer updated successfully")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PatchMapping(
        path = "/api/customers/{id}",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CASHIER', 'FINANCE')")
    ResponseEntity<WebResponse<CustomerDetailResponse>> updateCustomerAPI(@Parameter(description = "Customer ID") @PathVariable(name = "id") UUID id, @Valid @RequestBody CustomerUpdateRequest request){ 
        CustomerDetailResponse resultFromService = customerService.updateCustomer(id, request);

        WebResponse<CustomerDetailResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);
 
        return ResponseEntity.ok(wrappedResult);
    }
}
