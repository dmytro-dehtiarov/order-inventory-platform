package com.dmytro.orderinventoryplatform.shared.api;

import com.dmytro.orderinventoryplatform.shared.domain.ConflictException;
import com.dmytro.orderinventoryplatform.shared.domain.InvalidInputException;
import com.dmytro.orderinventoryplatform.shared.domain.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Single, centralized point for mapping exceptions thrown by any controller
 * in this application to HTTP responses.
 *
 * <p>All responses follow RFC 7807 ("Problem Details for HTTP APIs") via
 * Spring's {@link ProblemDetail}, and HTTP status codes follow RFC 9110
 * semantics: 404 for a missing resource, 409 for a state conflict, 400 for
 * a client request that fails validation.
 *
 * <p>A 400 can arrive by either of two routes. Bean Validation rejects a
 * malformed request body at the API boundary, raising
 * {@link org.springframework.web.bind.MethodArgumentNotValidException},
 * which is not handled explicitly here: the parent class,
 * {@link ResponseEntityExceptionHandler}, already maps it to a 400
 * {@link ProblemDetail}, since the exception itself implements Spring's
 * {@code ErrorResponse} contract. Past that boundary, a domain entity
 * enforcing its own invariant raises an
 * {@link com.dmytro.orderinventoryplatform.shared.domain.InvalidInputException},
 * which the handler below maps to the same status. The second route exists
 * so the invariant holds for callers that never pass through the API layer.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    /**
     * Maps any {@link ResourceNotFoundException} (or module-specific
     * subclass) to a 404 response, per RFC 9110: the requested resource
     * does not exist.
     *
     * @param ex the thrown exception; its message becomes the problem detail
     * @return a {@link ProblemDetail} with status 404 and the exception's message
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    /**
     * Maps any {@link ConflictException} (or module-specific subclass) to a
     * 409 response, per RFC 9110: the request conflicts with the current
     * state of the resource (for example, a domain invariant violation).
     *
     * @param ex the thrown exception; its message becomes the problem detail
     * @return a {@link ProblemDetail} with status 409 and the exception's message
     */
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Maps any {@link InvalidInputException} (or module-specific subclass)
     * to a 400 response, per RFC 9110: the request carries values that are
     * invalid in themselves.
     *
     * <p>Complements the automatic handling of
     * {@link org.springframework.web.bind.MethodArgumentNotValidException}
     * described above: Bean Validation rejects bad input at the API
     * boundary, while this handler covers the same class of failure when a
     * domain entity enforces the invariant itself.
     *
     * @param ex the thrown exception; its message becomes the problem detail
     * @return a {@link ProblemDetail} with status 400 and the exception's message
     */
    @ExceptionHandler(InvalidInputException.class)
    public ProblemDetail handleInvalidInput(InvalidInputException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
