package lumi.insert.app.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.controller.wrapper.WebResponse;
import lumi.insert.app.dto.request.PaginationRequest;
import lumi.insert.app.dto.request.StockCardCreateRequest;
import lumi.insert.app.dto.request.StockCardGetByFilter;
import lumi.insert.app.dto.response.StockCardResponse; 
import lumi.insert.app.service.StockCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Slf4j
@Tag(name = "Stock Cards", description = "Endpoints for managing stock card records and inventory tracking")
public class StockCardController {

    @Autowired
    StockCardService stockCardService;
    
    @Operation(summary = "Create new stock card", description = "Creates a new stock card record for inventory tracking")
    @ApiResponse(responseCode = "201", description = "Stock card created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping(
        path = "/api/stockcards",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    ResponseEntity<WebResponse<StockCardResponse>> createStockCardAPI(@Valid @RequestBody StockCardCreateRequest request){
        log.debug("Stock card creation request: {}", request);
        log.info("Register request for new stock card");
        
        StockCardResponse resultFromService = stockCardService.createStockCard(request);
        log.debug("Stock card created: {}", resultFromService);

        WebResponse<StockCardResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/" + resultFromService.id())
        .buildAndExpand(resultFromService.id())
        .toUri();

        log.info("Stock card created successfully with ID: {}", resultFromService.getId());
        return ResponseEntity.created(location).body(wrappedResult);
    }

    @Operation(summary = "Get stock card by ID", description = "Retrieve detailed information about a specific stock card")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved stock card")
    @ApiResponse(responseCode = "404", description = "Stock card not found")
    @GetMapping(
        path = "/api/stockcards/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<StockCardResponse>> getStockCardAPI(@Parameter(description = "Stock card ID") @PathVariable(name = "id") UUID id){
        log.debug("Stock card search by id request: {}", id);
        StockCardResponse resultFromService = stockCardService.getStockCard(id);
        log.debug("Stock card found: {}", resultFromService);

        WebResponse<StockCardResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Get all stock cards", description = "Retrieve paginated list of all stock cards with optional cursor-based pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved stock cards")
    @GetMapping(
        path = "/api/stockcards",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<Slice<StockCardResponse>>> getStockCardsAPI(@Parameter(description = "Last stock card ID for cursor-based pagination (optional)") @RequestParam(name = "lastId", required = false) UUID lastId, @ModelAttribute PaginationRequest request){
        log.debug("Stock cards list request with lastId: {}, pagination: {}", lastId, request);
        Slice<StockCardResponse> resultFromService = stockCardService.getStockCards(lastId, request);
        log.debug("Stock cards found: {}", resultFromService);

        WebResponse<Slice<StockCardResponse>> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Search stock cards", description = "Search stock cards with filtering options")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered stock cards")
    @GetMapping(
        path = "/api/stockcards/search",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<Slice<StockCardResponse>>> searchStockCardsAPI(@ModelAttribute @Valid StockCardGetByFilter request){
        log.debug("Stock cards search request with filter: {}", request);
        Slice<StockCardResponse> resultFromService = stockCardService.searchStockCards(request);
        log.debug("Stock cards found: {}", resultFromService);

        WebResponse<Slice<StockCardResponse>> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);
    }
}
