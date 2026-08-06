package com.dmytro.orderinventoryplatform.catalog.infrastructure;

import com.dmytro.orderinventoryplatform.catalog.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByCategoryId(Long category);

    Page<Product> findByActiveTrue(Pageable pageable);
}
