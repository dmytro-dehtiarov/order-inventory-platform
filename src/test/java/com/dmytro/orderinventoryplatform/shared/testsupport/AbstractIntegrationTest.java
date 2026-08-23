package com.dmytro.orderinventoryplatform.shared.testsupport;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Shared base class for integration tests that need a real Spring context
 * and a real database connection, instead of a unit-test in-memory object
 * graph.
 *
 * <p>Extending this class gives a test three things for free:
 * <ul>
 *   <li>A full Spring context ({@link SpringBootTest}), so real beans
 *       (repositories, the entity manager, etc.) are available.</li>
 *   <li>Connection to an isolated {@code test} schema/user (see
 *       {@code application-test.properties}), separate from the schema used
 *       for local development, with {@code spring.flyway.clean-disabled}
 *       set to {@code false} so Flyway can drop and recreate the schema on
 *       every run without touching real data.</li>
 *   <li>Automatic transaction rollback after each test method
 *       ({@link Transactional}), so tests never leak data into one another
 *       regardless of run order.</li>
 * </ul>
 *
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {
}