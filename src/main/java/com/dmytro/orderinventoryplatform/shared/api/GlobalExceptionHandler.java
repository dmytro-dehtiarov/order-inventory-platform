package com.dmytro.orderinventoryplatform.shared.api;

import com.dmytro.orderinventoryplatform.shared.domain.ConflictException;
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
 * <p>{@link org.springframework.web.bind.MethodArgumentNotValidException}
 * (raised by Bean Validation on {@code @Valid} request bodies) is not handled
 * explicitly here. It is mapped to a 400 {@link ProblemDetail} response
 * automatically by the parent class, {@link ResponseEntityExceptionHandler},
 * since the exception itself implements Spring's {@code ErrorResponse}
 * contract.
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
}
