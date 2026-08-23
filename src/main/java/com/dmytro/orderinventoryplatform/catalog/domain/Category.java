package com.dmytro.orderinventoryplatform.catalog.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;


/**
 * A product category, optionally nested under a parent category.
 *
 * <p>Carries no Bean Validation and no repository awareness: it only enforces
 * the invariants that must hold regardless of how the entity is constructed
 * (name must not be blank). Request-shape validation belongs to the API
 * layer, and cross-entity invariants such as "hierarchy must stay acyclic"
 * or "a category still referenced by children/products cannot be deleted"
 * are enforced by the application layer, since they require querying other
 * rows and cannot be checked from a single entity in isolation.
 */
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", nullable = true, length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parentCategory;

    @CreationTimestamp
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "updated_at", nullable = false, updatable = true)
    private Instant updatedAt;

    /**
     * No-args constructor required by JPA/Hibernate to reconstruct entities
     * from query results via reflection. Not intended for application code,
     * hence {@code protected} rather than {@code public}.
     */
    protected Category() {

    }

    /**
     * @param name a non-blank category name
     * @param description an optional, free-text description
     * @param parentCategory the parent category, or {@code null} for a
     *                        top-level category
     * @throws IllegalArgumentException if {@code name} is {@code null} or blank
     */
    public Category(String name, String description, Category parentCategory) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name;
        this.description = description;
        this.parentCategory = parentCategory;
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

    public Category getParentCategory() {
        return parentCategory;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
