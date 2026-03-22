package lumi.insert.app.controller;

import java.net.URI;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import lumi.insert.app.controller.wrapper.WebResponse;
import lumi.insert.app.core.entity.nondatabase.EmployeeLogin;
import lumi.insert.app.dto.request.MemoCreateRequest;
import lumi.insert.app.dto.request.MemoUpdateRequest;
import lumi.insert.app.dto.response.MemoResponse;
import lumi.insert.app.service.MemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Memos", description = "Endpoints for managing memos and announcements")
public class MemoController {
    
    @Autowired
    MemoService memoService;

    @Operation(summary = "Create new memo", description = "Creates a new memo with optional attachments")
    @ApiResponse(responseCode = "201", description = "Memo created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping(
        path = "/api/memos",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<WebResponse<MemoResponse>> createMemoAPI(@Valid @RequestBody MemoCreateRequest request){
        MemoResponse resultFromService = memoService.createMemo(request);

        WebResponse<MemoResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/" + resultFromService.id())
        .buildAndExpand(resultFromService.id())
        .toUri();

        return ResponseEntity.created(location).body(wrappedResult);
    }

    @Operation(summary = "Mark memo as read", description = "Records that an employee has read a specific memo")
    @ApiResponse(responseCode = "200", description = "Memo marked as read successfully")
    @ApiResponse(responseCode = "404", description = "Memo not found")
    @PostMapping(
        path = "/api/memos/{id}/read",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<Boolean>> createMemoViewAPI(@AuthenticationPrincipal EmployeeLogin login, @Parameter(description = "Memo ID") @PathVariable(name = "id") Long id){
        Boolean resultFromService = memoService.createMemoView(login, id);

        WebResponse<Boolean> wrappedResult = WebResponse.getWrapper(resultFromService, null);
 
        return ResponseEntity.ok(wrappedResult);
    }

    
    @Operation(summary = "Update memo", description = "Updates the content of an existing memo")
    @ApiResponse(responseCode = "200", description = "Memo updated successfully")
    @ApiResponse(responseCode = "404", description = "Memo not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PatchMapping(
        path = "/api/memos/{id}",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<MemoResponse>> updateMemoAPI(@Parameter(description = "Memo ID") @PathVariable(name = "id", required = true) Long id, @Valid @RequestBody MemoUpdateRequest request){
        MemoResponse resultFromService = memoService.updateMemo(id, request);

        WebResponse<MemoResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);
 
        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Archive memo", description = "Archives a memo to hide it from regular view (OWNER only)")
    @ApiResponse(responseCode = "200", description = "Memo archived successfully")
    @ApiResponse(responseCode = "404", description = "Memo not found")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @PreAuthorize("hasAnyRole('OWNER')")
    @PostMapping(
        path = "/api/memos/{id}/archive",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<MemoResponse>> archiveMemoAPI(@Parameter(description = "Memo ID") @PathVariable(name = "id") Long id){
        MemoResponse resultFromService = memoService.archiveMemo(id);

        WebResponse<MemoResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);
 
        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Get memo by ID", description = "Retrieve detailed information about a specific memo")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved memo")
    @ApiResponse(responseCode = "404", description = "Memo not found")
    @GetMapping(
        path = "/api/memos/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<MemoResponse>> getMemoAPI(@Parameter(description = "Memo ID") @PathVariable(name = "id") Long id){
        MemoResponse resultFromService = memoService.getMemo(id);

        WebResponse<MemoResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);
 
        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Get memos for current employee", description = "Retrieve paginated list of memos for the authenticated employee updated after specified time")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved memos")
    @GetMapping(
        path = "/api/memos",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<Slice<MemoResponse>>> getMemosAPI(@AuthenticationPrincipal EmployeeLogin login, @Parameter(description = "Filter memos updated after this date (ISO format, defaults to 1 month ago)") @RequestParam(name = "updatedAt", required = false) LocalDateTime time){
        if(time == null) time = LocalDateTime.now().minusMonths(1);
        Slice<MemoResponse> resultFromService = memoService.getMemos(login, time);

        WebResponse<Slice<MemoResponse>> wrappedResult = WebResponse.getWrapper(resultFromService, null);
 
        return ResponseEntity.ok(wrappedResult);
    }



}
