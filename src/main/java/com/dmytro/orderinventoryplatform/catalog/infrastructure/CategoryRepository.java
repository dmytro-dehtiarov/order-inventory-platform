package com.dmytro.orderinventoryplatform.catalog.infrastructure;

import com.dmytro.orderinventoryplatform.catalog.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
