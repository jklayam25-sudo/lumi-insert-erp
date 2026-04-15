package lumi.insert.app.service.implement;

import java.math.BigDecimal; 
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
 
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.aspect.annotation.ActivityLogger;
import lumi.insert.app.core.entity.Category;
import lumi.insert.app.core.entity.Product;
import lumi.insert.app.core.entity.nondatabase.ActivityAction;
import lumi.insert.app.core.entity.nondatabase.SliceIndex;
import lumi.insert.app.core.repository.CategoryRepository;
import lumi.insert.app.core.repository.ProductRepository;
import lumi.insert.app.core.repository.projection.ProductOutOfStock;
import lumi.insert.app.dto.request.PaginationRequest;
import lumi.insert.app.dto.request.ProductCreateRequest;
import lumi.insert.app.dto.request.ProductUpdateRequest;
import lumi.insert.app.dto.request.ProductGetByFilter;
import lumi.insert.app.dto.request.ProductGetNameRequest;
import lumi.insert.app.dto.response.ProductDeleteResponse;
import lumi.insert.app.dto.response.ProductName;
import lumi.insert.app.dto.response.ProductResponse;
import lumi.insert.app.dto.response.ProductStockResponse;
import lumi.insert.app.exception.BoilerplateRequestException;
import lumi.insert.app.exception.DuplicateEntityException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.mapper.ProductMapper;
import lumi.insert.app.service.ProductService;
import lumi.insert.app.utils.generator.JpaSpecGenerator;

@Service
@Transactional
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    JpaSpecGenerator jpaSpecGenerator;

    @Override
    @ActivityLogger(
        entityName = "products",
        action = ActivityAction.PRODUCT_CREATED,
        actionMessage = "New product created"
    )
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creating new product with name: {}", request.getName());
        if (productRepository.existsByName(request.getName())) {
            log.debug("Product creation failed - duplicate name: {}", request.getName());
            throw new DuplicateEntityException("Product with name " + request.getName() + " already exists");
        }

        Product newProduct = Product.builder()
            .name(request.getName())
            .basePrice(request.getBasePrice())
            .sellPrice(request.getSellPrice())
            .stockQuantity(request.getStockQuantity())
            .build();

        if(request.getStockMinimum() != null) {
            newProduct.setStockMinimum(request.getStockMinimum());
        }

        if(request.getCategoryId() != null) {
            log.debug("Product creation includes category ID: {}", request.getCategoryId());
            Category searchedCategory = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> {
                log.debug("Category not found for product creation with ID: {}", request.getCategoryId());
                return new NotFoundEntityException("Category with ID " + request.getCategoryId() + " was not found");
            });

            newProduct.setCategory(searchedCategory);
            searchedCategory.setTotalItems(searchedCategory.getTotalItems() + 1);
            categoryRepository.save(searchedCategory); 
        }

        Product savedProduct = productRepository.save(newProduct);
        log.debug("Product saved to database: {}", savedProduct);

        ProductResponse dtoResponseFromProduct = productMapper.createDtoResponseFromProduct(savedProduct);
        log.debug("Product response created: {}", dtoResponseFromProduct);
        return dtoResponseFromProduct;
    }


    @Override
    public ProductStockResponse getProductStock(Long productId) {
        log.debug("Getting stock for product ID: {}", productId);
        BigDecimal stock = productRepository.getStockById(productId).orElseThrow(() -> {
            log.debug("Product stock not found for ID: {}", productId);
            return new NotFoundEntityException("Product with ID " + productId + " was not found");
        });

        ProductStockResponse responseStock = ProductStockResponse.builder()
            .id(productId)
            .stockQuantity(stock)
            .build();

        return responseStock;
    }


    @Override
    @ActivityLogger(
        entityName = "products",
        action = ActivityAction.PRODUCT_UPDATED,
        actionMessage = "Product updated"
    )
    public ProductResponse updateProduct(ProductUpdateRequest request) {
        log.info("Updating product with ID: {}", request.getId());
        Product existingProduct = productRepository.findById(request.getId()).orElseThrow(() -> {
            log.debug("Product not found for update with ID: {}", request.getId());
            return new NotFoundEntityException("Product with ID " + request.getId() + " was not found");
        });

        productMapper.updateProductFromDto(request, existingProduct);
        Category category = existingProduct.getCategory();
        Long newCategoryId = request.getCategoryId();

        if(newCategoryId != null && (category == null || !category.getId().equals(newCategoryId))){
            if(category != null) {
                category.setTotalItems(category.getTotalItems() - 1L);
                categoryRepository.save(category);
            }

            Category newCategory = categoryRepository.findById(newCategoryId).orElseThrow(() -> {
                log.debug("Category not found for product update with ID: {}", newCategoryId);
                return new NotFoundEntityException("Category with ID " + newCategoryId + " was not found");
            });
            existingProduct.setCategory(newCategory);
            newCategory.setTotalItems(newCategory.getTotalItems() + 1L);

            categoryRepository.save(newCategory);
        } 

        Product updatedProduct = productRepository.save(existingProduct);

        ProductResponse dtoResponseFromProduct = productMapper.createDtoResponseFromProduct(updatedProduct);
        log.debug("Product updated and response created: {}", dtoResponseFromProduct);
        return dtoResponseFromProduct;
    }

    @Override
    public SliceIndex<ProductName> searchProductNames(ProductGetNameRequest request) {
        log.debug("Searching product names with query: {}, size: {}", request.getName(), request.getSize());
        if(request.getLastId() == null) request.setLastId(0L);
        Pageable pageable = PageRequest.of(0, request.getSize()).withSort(Sort.by("id").ascending());

        Slice<ProductName> allByNameContaining = productRepository.getByNameContainingIgnoreCaseAndIsActiveTrueAndIdAfter(request.getName(), request.getLastId(), pageable);
        log.debug("Found {} product names", allByNameContaining.getNumberOfElements());
        return new SliceIndex<ProductName>(allByNameContaining);
    }


    @Override
    public ProductResponse getProductById(Long id) {
        log.debug("Getting product by ID: {}", id);
        Product searchedProduct = productRepository.findById(id).orElseThrow(() -> {
            log.debug("Product not found with ID: {}", id);
            return new NotFoundEntityException("Product with ID " + id + " was not found");
        });
 
        ProductResponse responseProduct = productMapper.createDtoResponseFromProduct(searchedProduct);
        log.debug("Product response created: {}", responseProduct);
        return responseProduct;
    }


    @Override
    public Slice<ProductResponse> getProducts(PaginationRequest request) {
        log.debug("Getting products with pagination page: {}, size: {}", request.getPage(), request.getSize());
        Sort sort = Sort.by("name").ascending();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize()).withSort(sort);

        Slice<Product> allRawProducts = productRepository.findAllBy(pageable);
        log.debug("Found {} products", allRawProducts.getNumberOfElements());
        Slice<ProductResponse> mapResult = allRawProducts.map(productMapper::createDtoResponseFromProduct);
        return mapResult;
    }


    @Override
    public Slice<ProductResponse> getProductsByRequests(ProductGetByFilter request) {
        log.debug("Searching products by filter: {}", request);
        if(request.getCategoryId() != null && !(categoryRepository.existsById(request.getCategoryId()))){
            log.debug("Category filter ID not found: {}", request.getCategoryId());
            throw new NotFoundEntityException("Category with ID " + request.getCategoryId() + " was not found");
        }

        Pageable pageable = jpaSpecGenerator.pageable(request);
        Specification<Product> productSpecification = jpaSpecGenerator.productSpecification(request);

        Slice<Product> result = productRepository.findAll(productSpecification, pageable);
        log.debug("Found {} filtered products", result.getNumberOfElements());
        Slice<ProductResponse> resultMap = result.map(productMapper::createDtoResponseFromProduct);
        return resultMap;
    }


    @Override
    @ActivityLogger(
        entityName = "products",
        action = ActivityAction.PRODUCT_UPDATED,
        actionMessage = "Product set to inactive"
    )
    public ProductDeleteResponse deactivateProduct(Long id) {
        log.info("Deactivating product with ID: {}", id);
        Product searchedProduct = productRepository.findById(id).orElseThrow(() -> {
            log.debug("Product not found for deactivation with ID: {}", id);
            return new NotFoundEntityException("Category with ID " + id + " was not found");
        });
        if(!searchedProduct.getIsActive()) {
            log.debug("Product already inactive with ID: {}", id);
            throw new BoilerplateRequestException("Product with ID " + id + " already inactive");
        }

        Category category = searchedProduct.getCategory();

        if(category != null){
            category.setTotalItems(category.getTotalItems() - 1L);
            categoryRepository.save(category);
        }

        searchedProduct.setIsActive(false);
        Product savedProduct = productRepository.save(searchedProduct);

        ProductDeleteResponse deleteDtoResponseFromProduct = productMapper.createDeleteDtoResponseFromProduct(savedProduct);
        log.debug("Product deactivated and response created: {}", deleteDtoResponseFromProduct);
        return deleteDtoResponseFromProduct;
    }


    @Override
    @ActivityLogger(
        entityName = "products",
        action = ActivityAction.PRODUCT_UPDATED,
        actionMessage = "Product set to active"
    )
    public ProductDeleteResponse activateProduct(Long id) {
        log.info("Activating product with ID: {}", id);
        Product searchedProduct = productRepository.findById(id).orElseThrow(() -> {
            log.debug("Product not found for activation with ID: {}", id);
            return new NotFoundEntityException("Product with ID " + id + " was not found");
        });
        if(searchedProduct.getIsActive()) {
            log.debug("Product already active with ID: {}", id);
            throw new BoilerplateRequestException("Product with ID " + id + " already active");
        }

        Category category = searchedProduct.getCategory();

            if(category != null){
                category.setTotalItems(category.getTotalItems() + 1L);
                categoryRepository.save(category);
            }
        searchedProduct.setIsActive(true);
        Product savedProduct = productRepository.save(searchedProduct);

        ProductDeleteResponse deleteDtoResponseFromProduct = productMapper.createDeleteDtoResponseFromProduct(savedProduct);
        log.debug("Product activated and response created: {}", deleteDtoResponseFromProduct);
        return deleteDtoResponseFromProduct;
    }


    @Override
    public List<ProductOutOfStock> getOutOfStockProducts() {
        log.debug("Fetching out-of-stock products");
        List<ProductOutOfStock> outOfStockProducts = productRepository.findAllOutOfStockProduct();
        log.debug("Found {} out-of-stock products", outOfStockProducts.size());
        return outOfStockProducts;
    }

}
