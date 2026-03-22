package lumi.insert.app.controller;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;

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
import org.springframework.web.bind.annotation.PutMapping;
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
import lumi.insert.app.core.repository.projection.ProductOutOfStock;
import lumi.insert.app.dto.request.PaginationRequest;
import lumi.insert.app.dto.request.ProductCreateRequest;
import lumi.insert.app.dto.request.ProductUpdateRequest;
import lumi.insert.app.dto.request.ProductGetByFilter;
import lumi.insert.app.dto.request.ProductGetNameRequest;
import lumi.insert.app.dto.request.ProductStatisticExportRequest;
import lumi.insert.app.dto.response.ProductDeleteResponse;
import lumi.insert.app.dto.response.ProductName;
import lumi.insert.app.dto.response.ProductResponse;
import lumi.insert.app.dto.response.ProductStockResponse;
import lumi.insert.app.dto.response.TransactionItemStatisticResponse;
import lumi.insert.app.service.PdfService;
import lumi.insert.app.service.ProductService;
import lumi.insert.app.service.TransactionItemService;
import lumi.insert.app.utils.generator.DateUtils;

@RestController
@Slf4j
@Tag(name = "Products", description = "Endpoints for managing products and inventory")
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    PdfService pdfService;

    @Autowired
    TransactionItemService transactionItemService;

    @Autowired
    DateUtils dateUtils;
    
    @Operation(summary = "Get product by ID", description = "Retrieve detailed information about a specific product")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved product")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping(
        path = "/api/products/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<ProductResponse>> getProduct(@Parameter(description = "Product ID") @PathVariable(value = "id") Long id){
        ProductResponse resultFromService = productService.getProductById(id);

        WebResponse<ProductResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);   
    }

    @Operation(summary = "Search product names", description = "Search for products by name with pagination support")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved matching products")
    @GetMapping(
        path = "/api/products/searchName",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<SliceIndex<ProductName>>> searchProductNames(@Valid @ModelAttribute ProductGetNameRequest request){
        SliceIndex<ProductName> resultFromService = productService.searchProductNames(request);

        WebResponse<SliceIndex<ProductName>> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);   
    }

    @Operation(summary = "Get products by filter", description = "Retrieve paginated list of products with filtering options")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered products")
    @GetMapping(
        path = "/api/products/filter",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<Slice<ProductResponse>>> getProductByFilter(@ModelAttribute @Valid ProductGetByFilter request){
        Slice<ProductResponse> resultFromService = productService.getProductsByRequests(request);

        WebResponse<Slice<ProductResponse>> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);   
    }

    @Operation(summary = "Get all products", description = "Retrieve paginated list of all products")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved products")
    @GetMapping(
        path = "/api/products",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<Slice<ProductResponse>>> getProducts(@ModelAttribute PaginationRequest request){
        Slice<ProductResponse> resultFromService = productService.getProducts(request);

        WebResponse<Slice<ProductResponse>> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);   
    }

    @Operation(summary = "Get product stock information", description = "Retrieve detailed stock information for a specific product")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved product stock")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping(
        path = "/api/products/{id}/stocks",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<WebResponse<ProductStockResponse>> getProductStock(@Parameter(description = "Product ID") @PathVariable(value = "id", required = true) Long id ){
        ProductStockResponse resultFromService = productService.getProductStock(id);

        WebResponse<ProductStockResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);   
    }

    @Operation(summary = "Export products statistics to PDF", description = "Generates a PDF document of all products statistics (Best Seller, Top Refund, Out of stock list)")
    @ApiResponse(responseCode = "200", description = "Successfully exported supply order to PDF") 
    @GetMapping(
        path = "/api/products/statistics/export",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    @PreAuthorize("hasAnyRole('OWNER')")
    ResponseEntity<InputStreamResource> getProductsStatistics(@Valid @ModelAttribute ProductStatisticExportRequest request){ 
        if(request.getStartDate() == null) request.setStartDate(dateUtils.getFirstDateThisMonth());
        if(request.getEndDate() == null) request.setEndDate(dateUtils.getFirstDateNextMonth());
        TransactionItemStatisticResponse transactionItemStats = transactionItemService.getTransactionItemStats(request.getStartDate(), request.getEndDate());
        List<ProductOutOfStock> outOfStockProducts = productService.getOutOfStockProducts();

        ByteArrayInputStream pdf = pdfService.exportProductsStatistic(transactionItemStats, outOfStockProducts, request.getStartDate(), request.getEndDate());
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=product-statistic-"+ request.getStartDate() + request.getEndDate() + ".pdf");

        return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new InputStreamResource(pdf)); 
    }

    @Operation(summary = "Create new product", description = "Creates a new product with the specified details")
    @ApiResponse(responseCode = "201", description = "Product created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping(
        path = "/api/products",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @PreAuthorize("hasAnyRole('WAREHOUSE')")
    ResponseEntity<WebResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductCreateRequest request){
        ProductResponse resultFromService = productService.createProduct(request);

        WebResponse<ProductResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/" + resultFromService.id())
        .buildAndExpand(resultFromService.id())
        .toUri();

        return ResponseEntity.created(location).body(wrappedResult);   
    }

    @Operation(summary = "Activate product", description = "Activates a product to make it available in the system")
    @ApiResponse(responseCode = "200", description = "Product activated successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PostMapping(
        path = "/api/products/{id}/activate",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('WAREHOUSE')")
    ResponseEntity<WebResponse<ProductDeleteResponse>> activateProduct(@Parameter(description = "Product ID") @PathVariable(value = "id", required = true) Long id ){ 
        ProductDeleteResponse resultFromService = productService.activateProduct(id);

        WebResponse<ProductDeleteResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);   
    }

    @Operation(summary = "Deactivate product", description = "Deactivates a product to make it unavailable for new transactions")
    @ApiResponse(responseCode = "200", description = "Product deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PostMapping(
        path = "/api/products/{id}/deactivate",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('WAREHOUSE')")
    ResponseEntity<WebResponse<ProductDeleteResponse>> deactivateProduct(@Parameter(description = "Product ID") @PathVariable(value = "id", required = true) Long id ){ 
        ProductDeleteResponse resultFromService = productService.deactivateProduct(id);

        WebResponse<ProductDeleteResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);   
    }

    @Operation(summary = "Update product", description = "Updates information for an existing product")
    @ApiResponse(responseCode = "200", description = "Product updated successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PutMapping(
        path = "/api/products/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @PreAuthorize("hasAnyRole('WAREHOUSE')")
    ResponseEntity<WebResponse<ProductResponse>> editProduct(@Parameter(description = "Product ID") @PathVariable(value = "id", required = true) Long id, @Valid @RequestBody ProductUpdateRequest request){
        request.setId(id);
        ProductResponse resultFromService = productService.updateProduct(request);

        WebResponse<ProductResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        return ResponseEntity.ok(wrappedResult);   
    }

    


}
