package com.dmytro.orderinventoryplatform.catalog.infrastructure;

import com.dmytro.orderinventoryplatform.catalog.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByParentId(Long parentId);
}
