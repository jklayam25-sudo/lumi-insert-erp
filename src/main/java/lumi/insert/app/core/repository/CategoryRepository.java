package lumi.insert.app.core.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import lumi.insert.app.core.entity.Category;

/**
 * Repository for {@link Category} entity.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    boolean existsByName(String name);

    Slice<Category> findAllByNameContainingAndIsActiveTrue(String name, Pageable pageable);

    Slice<Category> findAllByIsActiveTrue(Pageable pageable);

    Slice<Category> findAllByIsActiveFalse(Pageable pageable);
}
