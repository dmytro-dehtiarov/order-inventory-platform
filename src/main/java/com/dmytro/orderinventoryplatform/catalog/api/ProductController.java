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

@RestController
@RequestMapping("/api/v1/catalog/products")
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

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

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        ProductResponse productResponse = productMapper.toResponse(product);

        return ResponseEntity.ok(productResponse);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(Pageable pageable) {
        Page<Product> products = productService.listProducts(pageable);
        Page<ProductResponse> productResponses = products.map(productMapper::toResponse);

        return ResponseEntity.ok(productResponses);
    }

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

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }

}
