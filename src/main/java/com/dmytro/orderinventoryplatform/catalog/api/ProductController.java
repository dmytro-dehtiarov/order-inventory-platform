package com.dmytro.orderinventoryplatform.catalog.api;

import com.dmytro.orderinventoryplatform.catalog.application.ProductService;
import com.dmytro.orderinventoryplatform.catalog.domain.Product;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST controller for {@link Product}: exposes CRUD endpoints at
 * {@code /api/v1/catalog/products}, delegating all business logic and
 * validation to {@link ProductService} and mapping domain objects to/from
 * DTOs via {@link ProductMapper}.
 *
 * <p>There is no physical delete endpoint - {@link Product} only supports
 * soft-deletion via {@link #deactivateProduct}, since {@code ProductService}
 * has no hard-delete operation.
 *
 * <p>Error handling (404/400) is not done here - it relies entirely on the
 * exceptions thrown by {@code ProductService} being caught by the
 * application-wide {@code GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/v1/catalog/products")
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    /**
     * @param productRequest the product to create; see {@link ProductRequest}
     *                        for field-level validation
     * @return {@code 201 Created}, with a {@code Location} header pointing
     *         at the new product and the created product in the body
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        Product product = productService.createProduct(
                productRequest.name(),
                productRequest.description(),
                productRequest.price(),
                productRequest.categoryId(),
                productRequest.active()
        );

        ProductResponse productResponse = productMapper.toResponse(product);

        UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}");

        return ResponseEntity.created(builder.buildAndExpand(productResponse.id()).toUri()).body(productResponse);
    }

    /**
     * @param id the product id
     * @return {@code 200 OK} with the product in the body
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        ProductResponse productResponse = productMapper.toResponse(product);

        return ResponseEntity.ok(productResponse);
    }

    /**
     * @param pageable pagination and sorting parameters, resolved
     *                  automatically from {@code page}/{@code size}/
     *                  {@code sort} query parameters
     * @return {@code 200 OK} with a page of products in the body; per
     *         {@link ProductService#listProducts}, only {@code active}
     *         products are included
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(Pageable pageable) {
        Page<Product> products = productService.listProducts(pageable);
        Page<ProductResponse> productResponses = products.map(productMapper::toResponse);

        return ResponseEntity.ok(productResponses);
    }

    /**
     * Full-replace update: every field of {@code productRequest} is
     * applied, not merged field-by-field against the existing product.
     *
     * @param id the id of the product to update
     * @param productRequest the new field values; see {@link ProductRequest}
     *                        for field-level validation
     * @return {@code 200 OK} with the updated product in the body
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest) {
        Product product = productService.updateProduct(
                id,
                productRequest.name(),
                productRequest.description(),
                productRequest.price(),
                productRequest.categoryId(),
                productRequest.active()
        );
        ProductResponse productResponse = productMapper.toResponse(product);
        return ResponseEntity.ok(productResponse);
    }

    /**
     * Soft-delete: sets {@code active=false} via
     * {@link ProductService#deactivateProduct}, idempotent.
     *
     * @param id the id of the product to deactivate
     * @return {@code 204 No Content}
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }

}
