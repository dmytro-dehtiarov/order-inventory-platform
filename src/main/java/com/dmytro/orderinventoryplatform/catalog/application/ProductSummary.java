package com.dmytro.orderinventoryplatform.catalog.application;

import java.math.BigDecimal;

/**
 * Read-model DTO for {@link Product}, returned only by
 * {@link ProductService#getSummaries}, catalog's one cross-module read API.
 *
 * <p>Deliberately narrower than {@code Product}: carries only the fields the
 * {@code orders} module actually needs when creating an order (validate
 * existence/active status, snapshot {@code name}/{@code price} into the
 * order line at purchase time). No {@code description} — {@code orders}
 * never displays it, and a cross-module contract should carry only what its
 * consumer uses, not everything the producer happens to have.
 */
public record ProductSummary(Long id, String name, BigDecimal price, boolean active) {}