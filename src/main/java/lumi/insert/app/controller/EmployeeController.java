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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lumi.insert.app.controller.wrapper.WebResponse;
import lumi.insert.app.dto.request.EmployeeCreateRequest;
import lumi.insert.app.dto.request.EmployeeUpdateRequest;
import lumi.insert.app.dto.request.PaginationRequest;
import lumi.insert.app.dto.response.EmployeeResponse;
import lumi.insert.app.service.EmployeeService;

@RestController 
@Slf4j
@Tag(name = "Employees", description = "Endpoints for managing employee accounts and authentication")
public class EmployeeController {
    
    @Autowired
    EmployeeService employeeService;

    @Operation(summary = "Create new employee", description = "Creates a new employee account with the specified details")
    @ApiResponse(responseCode = "201", description = "Employee created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping(
        path = "/api/employees",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @PreAuthorize("hasAnyRole('OWNER')")
    ResponseEntity<WebResponse<EmployeeResponse>> createEmployeeAPI(@Valid @RequestBody EmployeeCreateRequest request){
        EmployeeResponse resultFromService = employeeService.createEmployee(request);

        WebResponse<EmployeeResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/" + resultFromService.id())
        .buildAndExpand(resultFromService.id())
        .toUri();

        return ResponseEntity.created(location).body(wrappedResult);
    }

    @Operation(summary = "Get employee by ID", description = "Retrieve detailed information about a specific employee")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved employee")
    @ApiResponse(responseCode = "404", description = "Employee not found")
    @GetMapping(
        path = "/api/employees/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<EmployeeResponse>> getEmployeeAPI(@Parameter(description = "Employee ID") @PathVariable(name = "id") UUID id){
        EmployeeResponse resultFromService = employeeService.getEmployee(id);

        WebResponse<EmployeeResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Get all employees", description = "Retrieve paginated list of all employees")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved employees")
    @GetMapping(
        path = "/api/employees",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<Slice<EmployeeResponse>>> getEmployeesAPI(@Valid @ModelAttribute PaginationRequest request){
        
        Slice<EmployeeResponse> resultFromService = employeeService.getEmployees(request);

        WebResponse<Slice<EmployeeResponse>> wrappedResult = WebResponse.getWrapper(resultFromService, null);
 
        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Check if username exists", description = "Check if a username is already registered in the system")
    @ApiResponse(responseCode = "200", description = "Successfully checked username availability")
    @GetMapping(
        path = "/api/employees/exists",
        produces = MediaType.APPLICATION_JSON_VALUE,
        params = "username"
    )
    ResponseEntity<WebResponse<Boolean>> isUsernameExistsAPI(@Parameter(description = "Username to check") String username){
        boolean resultFromService = employeeService.isExistsEmployeeByUsername(username);

        WebResponse<Boolean> wrappedResult = WebResponse.getWrapper(resultFromService, null); 
        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Reset employee password", description = "Reset an employee's password to a new value")
    @ApiResponse(responseCode = "200", description = "Password reset successfully")
    @ApiResponse(responseCode = "404", description = "Employee not found")
    @ApiResponse(responseCode = "400", description = "Invalid password")
    @PostMapping(
        path = "/api/employees/{id}/reset",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @PreAuthorize("hasAnyRole('OWNER')")
    ResponseEntity<WebResponse<EmployeeResponse>> resetEmployeePasswordAPI(@Parameter(description = "Employee ID") @PathVariable(name = "id") UUID id, @RequestBody @Valid @RequestParam(name = "password") @Pattern(regexp = "^(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{5,50}$", message = "password has to be 5-50 length and has atleast 1 unique char") String password){
        EmployeeResponse resultFromService = employeeService.resetEmployeePassword(id, password);

        WebResponse<EmployeeResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);
 
        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Update employee", description = "Updates information for an existing employee")
    @ApiResponse(responseCode = "200", description = "Employee updated successfully")
    @ApiResponse(responseCode = "404", description = "Employee not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PatchMapping(
        path = "/api/employees/{id}",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('OWNER')")
    ResponseEntity<WebResponse<EmployeeResponse>> updateEmployeeAPI(@Parameter(description = "Employee ID") @PathVariable(name = "id") UUID id, @Valid @RequestBody EmployeeUpdateRequest request){ 
        EmployeeResponse resultFromService = employeeService.updateEmployee(id, request);

        WebResponse<EmployeeResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);
 
        return ResponseEntity.ok(wrappedResult);
    }

}
