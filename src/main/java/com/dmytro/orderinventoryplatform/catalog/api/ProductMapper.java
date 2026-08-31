package com.dmytro.orderinventoryplatform.catalog.api;

import com.dmytro.orderinventoryplatform.catalog.domain.Product;
import org.springframework.stereotype.Component;

/**
 * Manual mapper between {@link Product} and {@link ProductResponse}.
 */
@Component
public class ProductMapper {
    /**
     * @param product the entity to map; its {@code category} association
     *                 must be non-null, per the {@code Product} domain
     *                 invariant
     * @return the equivalent response DTO, with {@code categoryId} resolved
     *         from the category association
     */
    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory().getId(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}