package lumi.insert.app.service.implement;
 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.aspect.annotation.ActivityLogger;
import lumi.insert.app.core.entity.Category;
import lumi.insert.app.core.entity.nondatabase.ActivityAction;
import lumi.insert.app.core.repository.CategoryRepository;
import lumi.insert.app.dto.request.CategoryCreateRequest;
import lumi.insert.app.dto.request.CategoryUpdateRequest;
import lumi.insert.app.dto.request.PaginationRequest;
import lumi.insert.app.dto.response.CategoryResponse;
import lumi.insert.app.exception.BoilerplateRequestException;
import lumi.insert.app.exception.DuplicateEntityException;
import lumi.insert.app.exception.NotFoundEntityException;
import lumi.insert.app.mapper.CategoryMapper;
import lumi.insert.app.service.CategoryService;

@Service
@Transactional
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    CategoryMapper categoryMapper;

    @Override
    @ActivityLogger(
        entityName = "categories",
        action = ActivityAction.CATEGORY_CREATED,
        actionMessage = "Category created"
    )
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        log.info("Creating category with name: {}", request.getName());

        if(categoryRepository.existsByName(request.getName())){
            throw  new DuplicateEntityException("Category with name " + request.getName() + " already exists");
        } else {
            Category newCategory = Category.builder()
                .name(request.getName())
                .build();

            Category savedCategory = categoryRepository.save(newCategory);
            log.debug("Category saved to database: {}", savedCategory);

            CategoryResponse response = categoryMapper.createDtoResponseFromCategory(savedCategory);
            log.debug("Category response created: {}", response);

            return response;
        }
    }

    @Override 
    @ActivityLogger(
        entityName = "categories",
        action = ActivityAction.CATEGORY_UPDATED,
        actionMessage = "Category name updated"
    )
    public CategoryResponse updateCategoryName(CategoryUpdateRequest request) {
        log.info("Updating category name for ID: {} to: {}", request.getId(), request.getName());

        if(categoryRepository.existsByName(request.getName())){
            throw  new DuplicateEntityException("Category with name " + request.getName() + " already exists");
        } else {
        Category searchedCategory = categoryRepository.findById(request.getId()).orElseThrow(() -> new NotFoundEntityException("Category with ID " + request.getId() + " was not found"));
        
        searchedCategory.setName(request.getName());
        Category savedCategory = categoryRepository.save(searchedCategory);
        log.debug("Category updated in database: {}", savedCategory);

        CategoryResponse response = categoryMapper.createDtoResponseFromCategory(savedCategory);
        log.debug("Category response created: {}", response);
        return response;
        }
    }

    @Override
    @ActivityLogger(
        entityName = "categories",
        action = ActivityAction.CATEGORY_UPDATED,
        actionMessage = "Category status set to active"
    )
    public CategoryResponse activateCategory(Long id) {
        log.info("Activating category with ID: {}", id);

        Category searchedCategory = categoryRepository.findById(id).orElseThrow(() -> new NotFoundEntityException("Category with ID " + id + " was not found"));
        if(searchedCategory.getIsActive()) throw new BoilerplateRequestException("Category with ID " + id + " already active");

        searchedCategory.setIsActive(true);
        Category savedCategory = categoryRepository.save(searchedCategory);
        log.debug("Category activated in database: {}", savedCategory);

        CategoryResponse response = categoryMapper.createDtoResponseFromCategory(savedCategory);
        log.debug("Category response created: {}", response);
        return response;
    }

    @Override
    @ActivityLogger(
        entityName = "categories",
        action = ActivityAction.CATEGORY_UPDATED,
        actionMessage = "Category status set to inactive"
    )
    public CategoryResponse deactivateCategory(Long id) {
        log.info("Deactivating category with ID: {}", id);

        Category searchedCategory = categoryRepository.findById(id).orElseThrow(() -> new NotFoundEntityException("Category with ID " + id + " was not found"));
        if(!searchedCategory.getIsActive()) throw new BoilerplateRequestException("Category with ID " + id + " already inactive");

        searchedCategory.setIsActive(false);
        Category savedCategory = categoryRepository.save(searchedCategory);
        log.debug("Category deactivated in database: {}", savedCategory);
        
        CategoryResponse response = categoryMapper.createDtoResponseFromCategory(savedCategory);
        log.debug("Category response created: {}", response);
        return response;
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        log.debug("Getting category by ID: {}", id);

        Category searchedCategory = categoryRepository.findById(id).orElseThrow(() -> new NotFoundEntityException("Category with ID " + id + " was not found"));
        log.debug("Category found: {}", searchedCategory);

        CategoryResponse response = categoryMapper.createDtoResponseFromCategory(searchedCategory);
        log.debug("Category response created: {}", response);

        return response;
    }

    @Override
    public Slice<CategoryResponse> getCategories(PaginationRequest request) {
        log.debug("Getting categories with pagination - page: {}, size: {}", request.getPage(), request.getSize());

        Sort sort = Sort.by("name").ascending();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize()).withSort(sort);

        Slice<Category> searchedCategories = categoryRepository.findAllByIsActiveTrue(pageable);
        log.debug("Found {} categories", searchedCategories.getNumberOfElements());

        Slice<CategoryResponse> response = searchedCategories.map(categoryMapper::createDtoResponseFromCategory);
        log.debug("Category responses created, total: {}", response.getNumberOfElements());

        return response;
    }
    
}

 