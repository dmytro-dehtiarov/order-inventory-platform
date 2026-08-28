package com.dmytro.orderinventoryplatform.catalog.infrastructure;

import com.dmytro.orderinventoryplatform.catalog.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Category}.
 *
 * <p>Derived query methods only, no custom implementations. Pagination is
 * already provided by {@link JpaRepository#findAll(org.springframework.data.domain.Pageable)},
 * inherited without needing to be redeclared here.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * @param parentCategoryId the {@code id} of a category
     * @return {@code true} if at least one category has {@code parentCategoryId} as
     *         its parent; used as a delete guard so a category with children
     *         cannot be removed
     */
    boolean existsByParentCategoryId(Long parentCategoryId);

    /**
     * @param categoryId the {@code id} of a category
     * @return the {@code id} of that category's parent, or an empty
     *         {@code Optional} if the category is top-level (has no parent).
     *         Custom {@code @Query} because this returns a single scalar field
     *         of the related entity, not the entity itself — something a
     *         derived query method name can't express directly.
     *         <p>Used by {@code CategoryService} to walk the ancestor chain
     *         one id at a time when checking whether reassigning a category's
     *         parent would create a cycle in the hierarchy.
     */
    @Query("SELECT c.parentCategory.id FROM Category c WHERE c.id = :categoryId")
    Optional<Long> findParentCategoryIdByCategoryId(@Param("categoryId") Long categoryId);
}
