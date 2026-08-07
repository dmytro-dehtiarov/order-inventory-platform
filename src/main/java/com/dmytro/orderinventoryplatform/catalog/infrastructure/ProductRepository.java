package com.dmytro.orderinventoryplatform.catalog.infrastructure;

import com.dmytro.orderinventoryplatform.catalog.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Product}.
 *
 * <p>Derived query methods only, no custom implementations. Pagination is
 * already provided by {@link JpaRepository#findAll(Pageable)}, inherited
 * without needing to be redeclared here.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * @param categoryId the {@code id} of a category
     * @return {@code true} if at least one product belongs to that category;
     *         used as a delete guard so a category still holding products
     *         cannot be removed
     */
    boolean existsByCategoryId(Long categoryId);

    /**
     * @param pageable pagination and sorting parameters
     * @return a page of active products only
     */
    Page<Product> findByActiveTrue(Pageable pageable);
}
