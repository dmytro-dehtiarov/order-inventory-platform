package com.dmytro.orderinventoryplatform.catalog.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A sellable product, always assigned to exactly one {@link Category}.
 *
 * <p>Carries no Bean Validation and no repository awareness: it only enforces
 * the invariants that must hold regardless of how the entity is constructed
 * (name must not be blank, price must not be negative, category must be
 * present). Request-shape validation belongs to the API layer.
 */
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", nullable = true, length = 2000)
    private String description;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, updatable = true)
    private Instant updatedAt;

    /**
     * No-args constructor required by JPA/Hibernate to reconstruct entities
     * from query results via reflection. Not intended for application code,
     * hence {@code protected} rather than {@code public}.
     */
    protected Product() {

    }

    /**
     * @param name a non-blank product name
     * @param description an optional, free-text description
     * @param price the unit price; must not be {@code null} or negative
     * @param category the category this product belongs to; required
     * @param active whether the product is currently active/sellable;
     *               required, since {@code active} is a primitive concern in
     *               the database and callers must state it explicitly
     * @throws IllegalArgumentException if {@code name} is blank, {@code price}
     *                                   is {@code null} or negative, or
     *                                   {@code category}/{@code active} is
     *                                   {@code null}
     */
    public Product (String name, String description, BigDecimal price, Category category, Boolean active) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
        if (category == null) {
            throw new IllegalArgumentException("product must have a category");
        }
        if (active == null) {
            throw new IllegalArgumentException("product must have an active status");
        }
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isActive() {
        return active;
    }

    public Category getCategory() {
        return category;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

}
